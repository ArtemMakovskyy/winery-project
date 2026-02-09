package com.winestoreapp.wineryadminui.features.auth;

import com.winestoreapp.wineryadminui.core.security.SessionTokenStorage;
import com.winestoreapp.wineryadminui.features.user.dto.UserLoginRequestDto;
import com.winestoreapp.wineryadminui.features.user.dto.UserLoginResponseDto;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Mock
    private AuthFeignClient authFeignClient;

    @Mock
    private SessionTokenStorage storage;

    @Mock
    private Tracer tracer;

    @Mock
    private Span span;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lenient().when(tracer.currentSpan()).thenReturn(span);
        lenient().when(session.getId()).thenReturn("session-123");
    }

    @Test
    void login_success_shouldSaveTokenAndTagSpan() {
        // given
        var dto = new UserLoginRequestDto("user@example.com", "password");
        var response = new UserLoginResponseDto("jwt-token-123");
        when(authFeignClient.login(dto)).thenReturn(response);

        // when
        authService.login(dto, session);

        // then
        verify(authFeignClient).login(dto);
        verify(storage).save(session, "jwt-token-123");
        verify(span).tag("status", "success");
        verify(span).tag("user.email", "user@example.com");
    }

    @Test
    void login_failure_shouldThrowExceptionAndNotSaveToken() {
        // given
        var dto = new UserLoginRequestDto("user@example.com", "wrong");
        RuntimeException backendError = new RuntimeException("Backend error");
        when(authFeignClient.login(dto)).thenThrow(backendError);

        // when / then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(dto, session));

        assertThat(ex.getMessage()).contains("Login failed");
        verify(storage, never()).save(any(), any());
        verify(span).tag("status", "error");
        verify(span).error(backendError);
    }

    @Test
    void logout_success_shouldClearSession() {
        // when
        authService.logout(session);

        // then
        verify(storage).clear(session);
        verify(span).tag("status", "success");
    }

    @Test
    void logout_failure_shouldThrowException() {
        // given
        RuntimeException storageError = new RuntimeException("Storage error");
        doThrow(storageError).when(storage).clear(session);

        // when / then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.logout(session));

        assertThat(ex.getMessage()).contains("Logout failed");
        verify(span).tag("status", "error");
        verify(span).error(storageError);
    }
}