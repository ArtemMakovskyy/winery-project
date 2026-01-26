package com.winestoreapp.wineryadminui.core.config;

import com.winestoreapp.wineryadminui.core.security.SessionTokenStorage;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import feign.RequestInterceptor;

@Configuration
@RequiredArgsConstructor
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
                        template.header("Authorization", "Bearer " + token);
                    }
                }
            }
        };
    }
}
