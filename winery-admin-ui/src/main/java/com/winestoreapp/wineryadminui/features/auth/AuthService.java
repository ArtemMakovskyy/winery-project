package com.winestoreapp.wineryadminui.features.auth;

import com.winestoreapp.wineryadminui.core.security.SessionTokenStorage;
import com.winestoreapp.wineryadminui.features.user.dto.UserLoginRequestDto;
import com.winestoreapp.wineryadminui.features.user.dto.UserLoginResponseDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthFeignClient authFeignClient;
    private final SessionTokenStorage storage;
    private final Tracer tracer;

    @Observed(
            name = "auth.service.login",
            lowCardinalityKeyValues = {"operation", "login"}
    )
    public void login(UserLoginRequestDto dto, HttpSession session) {
        log.info("Attempting login for email: {}", dto.email());
        tagSpan("user.email", dto.email());

        try {
            UserLoginResponseDto response = authFeignClient.login(dto);
            storage.save(session, response.token());
            log.info("Authentication successful. Session ID: {}", session.getId());

            tagSpan("status", "success");
        } catch (Exception e) {
            log.error("Authentication failed for email: {}", dto.email(), e);

            tagSpan("status", "error");
            recordError(e);

            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    @Observed(
            name = "auth.service.logout",
            lowCardinalityKeyValues = {"operation", "logout"}
    )
    public void logout(HttpSession session) {
        log.info("Logging out session: {}", session.getId());

        try {
            storage.clear(session);
            tagSpan("status", "success");
        } catch (Exception e) {
            log.error("Logout failed for session: {}", session.getId(), e);

            tagSpan("status", "error");
            recordError(e);

            throw new RuntimeException("Logout failed: " + e.getMessage(), e);
        }
    }

    private void tagSpan(String key, Object value) {
        var span = tracer.currentSpan();
        if (span != null) {
            span.tag(key, String.valueOf(value));
        }
    }

    private void recordError(Throwable e) {
        var span = tracer.currentSpan();
        if (span != null) {
            span.error(e);
        }
    }
}