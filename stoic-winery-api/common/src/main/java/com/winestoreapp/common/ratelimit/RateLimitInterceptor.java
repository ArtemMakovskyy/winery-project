package com.winestoreapp.common.ratelimit;

import com.winestoreapp.common.exception.RateLimitException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Real-IP";

    public static final String HEADER_RATE_LIMIT = "X-RateLimit-Limit";
    public static final String HEADER_RATE_LIMIT_REMAINING = "X-RateLimit-Remaining";
    public static final String HEADER_RETRY_AFTER = "Retry-After";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }

        RateLimit config = method.getMethodAnnotation(RateLimit.class);
        if (config == null) {
            return true;
        }

        return handleRateLimit(request, response, method, config);
    }

    private boolean handleRateLimit(HttpServletRequest request,
                                    HttpServletResponse response,
                                    HandlerMethod method,
                                    RateLimit config) {

        String key = buildRateLimitKey(request, method);
        log.debug("[RATE-LIMIT] Checking rate limit for key: {}", key);

        RateLimitResult result = rateLimitService.check(
                key,
                config.maxRequests(),
                config.timeWindowSeconds()
        );

        log.debug("[RATE-LIMIT] Result for key '{}': allowed={}, count={}", 
                key, result.allowed(), result.currentCount());

        applyBaseHeaders(response, config);

        if (!result.allowed()) {
            log.warn("Rate limit exceeded for key: {}. Limit: {} requests per {} seconds", 
                    key, config.maxRequests(), config.timeWindowSeconds());
            applyExceededHeaders(response, config);
            throw createRateLimitException(config);
        }

        applySuccessHeaders(response, config, result.currentCount());
        log.debug("[RATE-LIMIT] Request allowed. Remaining: {} requests", 
                config.maxRequests() - (int) result.currentCount());

        return true;
    }

    private String buildRateLimitKey(HttpServletRequest request, HandlerMethod method) {
        String ip = getClientIp(request);
        String className = method.getBeanType().getSimpleName();
        String methodName = method.getMethod().getName();
        return ip + ":" + className + ":" + methodName;
    }

    private void applyBaseHeaders(HttpServletResponse response, RateLimit config) {
        response.setHeader(HEADER_RATE_LIMIT, String.valueOf(config.maxRequests()));
    }

    private void applyExceededHeaders(HttpServletResponse response, RateLimit config) {
        response.setHeader(HEADER_RATE_LIMIT_REMAINING, "0");
        response.setHeader(HEADER_RETRY_AFTER, String.valueOf(config.timeWindowSeconds()));
    }

    private void applySuccessHeaders(HttpServletResponse response,
                                     RateLimit config,
                                     long currentCount) {

        int remaining = config.maxRequests() - (int) currentCount;

        response.setHeader(
                HEADER_RATE_LIMIT_REMAINING,
                String.valueOf(Math.max(0, remaining))
        );
    }

    private RateLimitException createRateLimitException(RateLimit config) {
        return new RateLimitException(
                String.format(
                        "Rate limit exceeded. Max %d requests per %d seconds. Retry after %d seconds.",
                        config.maxRequests(),
                        config.timeWindowSeconds(),
                        config.timeWindowSeconds()
                )
        );
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader(X_FORWARDED_FOR);
        if (xff != null && !xff.isBlank() && !"unknown".equalsIgnoreCase(xff)) {
            return xff.split(",")[0].trim();
        }

        String xri = request.getHeader(X_REAL_IP);
        if (xri != null && !xri.isBlank() && !"unknown".equalsIgnoreCase(xri)) {
            return xri;
        }

        return request.getRemoteAddr();
    }
}
