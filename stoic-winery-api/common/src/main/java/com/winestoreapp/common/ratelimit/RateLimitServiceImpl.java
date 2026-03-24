package com.winestoreapp.common.ratelimit;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private final StringRedisTemplate redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    private static final String RATE_LIMIT_SCRIPT = """
            local key = KEYS[1]
            local maxRequests = tonumber(ARGV[1])
            local timeWindow = tonumber(ARGV[2])
            
            local current = redis.call('INCR', key)
            
            if current == 1 then
                redis.call('EXPIRE', key, timeWindow)
                return current
            end
            
            local ttl = redis.call('TTL', key)
            if ttl == -1 then
                redis.call('EXPIRE', key, timeWindow)
            end
            
            if current > maxRequests then
                return -2
            end
            
            return current
            """;

    @Override
    public RateLimitResult check(String key, int maxRequests, int windowSeconds) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        log.debug("[RATE-LIMIT] Checking rate limit for Redis key: {}", redisKey);

        try {
            Long result = redisTemplate.execute(
                    new DefaultRedisScript<>(RATE_LIMIT_SCRIPT, Long.class),
                    Collections.singletonList(redisKey),
                    String.valueOf(maxRequests),
                    String.valueOf(windowSeconds)
            );

            log.debug("[RATE-LIMIT] Lua script result for key '{}': {}", redisKey, result);

            if (result == null || result == -2) {
                log.debug("[RATE-LIMIT] Rate limit exceeded for key '{}'", redisKey);
                return new RateLimitResult(false, maxRequests);
            }

            log.debug("[RATE-LIMIT] Request allowed for key '{}'. Current count: {}", redisKey, result);
            return new RateLimitResult(true, result);

        } catch (Exception e) {
            log.error("[RATE-LIMIT] Redis error for key '{}'. Allowing request by default (fail-open).",
                    redisKey, e);
            return new RateLimitResult(true, 0); // Fail open
        }
    }

    public void reset(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        redisTemplate.delete(redisKey);
        log.debug("Rate limit key '{}' reset", redisKey);
    }
}
