package com.winestoreapp.wineryadminui.features.auth;

import com.winestoreapp.wineryadminui.core.security.SessionTokenStorage;
import com.winestoreapp.wineryadminui.features.user.dto.UserLoginRequestDto;
import com.winestoreapp.wineryadminui.features.user.dto.UserLoginResponseDto;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        var span = tracer.currentSpan();
        try {
            log.info("Attempting login for email: {}", dto.email());
            UserLoginResponseDto response = authFeignClient.login(dto);
            storage.save(session, response.token());
            log.info("Authentication successful. Session ID: {}", session.getId());

            if (span != null) {
                span.tag("status", "success");
                span.tag("user.email", dto.email());
            }
        } catch (Exception e) {
            log.error("Authentication failed for email: {}", dto.email(), e);
            if (span != null) {
                span.tag("status", "error");
                span.error(e);
                span.tag("user.email", dto.email());
            }
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    @Observed(
            name = "auth.service.logout",
            lowCardinalityKeyValues = {"operation", "logout"}
    )
    public void logout(HttpSession session) {
        var span = tracer.currentSpan();
        try {
            log.info("Logging out session: {}", session.getId());
            storage.clear(session);
            if (span != null) span.tag("status", "success");
        } catch (Exception e) {
            log.error("Logout failed for session: {}", session.getId(), e);
            if (span != null) {
                span.tag("status", "error");
                span.error(e);
            }
            throw new RuntimeException("Logout failed: " + e.getMessage(), e);
        }
    }
}
