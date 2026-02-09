package com.winestoreapp.wineryadminui.core.config;

import com.winestoreapp.wineryadminui.core.security.SessionTokenStorage;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import feign.RequestInterceptor;
import io.micrometer.tracing.Tracer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class FeignAuthInterceptor {

    private final SessionTokenStorage storage;
    private final Tracer tracer;

    @Bean
    public RequestInterceptor authRequestInterceptor() {
        return template -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attrs != null) {
                HttpSession session = attrs.getRequest().getSession(false);
                if (session != null) {
                    String token = storage.get(session);

                    if (token != null && !template.url().contains("/health")) {
                        tagSpan("session.id", session.getId());
                        template.header("Authorization", "Bearer " + token);
                        log.debug("Feign interceptor: Authorization header added for URL: {}", template.url());
                    }
                }
            }
        };
    }

    private void tagSpan(String key, String value) {
        if (tracer.currentSpan() != null) {
            tracer.currentSpan().tag(key, value);
        }
    }
}