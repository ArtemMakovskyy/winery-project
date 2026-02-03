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

@Configuration
@RequiredArgsConstructor
@Slf4j
public class FeignAuthInterceptor {

    private final SessionTokenStorage storage;

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
                        log.debug("Injecting Bearer token for request to: {}", template.url());
                        template.header("Authorization", "Bearer " + token);
                    } else if (token == null) {
                        log.warn("Attempted Feign request to {} but no token found in session", template.url());
                    }
                }
            }
        };
    }
}