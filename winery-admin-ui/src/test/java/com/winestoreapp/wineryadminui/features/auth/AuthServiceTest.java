package com.winestoreapp.wineryadminui.features.auth;

import com.winestoreapp.wineryadminui.core.observability.ObservationTags;
import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import com.winestoreapp.wineryadminui.core.security.SessionTokenStorage;
import com.winestoreapp.wineryadminui.features.user.dto.UserLoginRequestDto;
import com.winestoreapp.wineryadminui.features.user.dto.UserLoginResponseDto;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Mock
    private AuthFeignClient authFeignClient;

    @Mock
    private SessionTokenStorage storage;

    @Mock
    private SpanTagger spanTagger;

    @Mock
    private HttpSession session;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(authFeignClient, storage, spanTagger);
    }

    @Test
    void login_success_shouldSaveTokenAndTagSuccess() {
        var dto = new UserLoginRequestDto("user@example.com", "password");
        var response = new UserLoginResponseDto("jwt-token-123");

        when(authFeignClient.login(dto)).thenReturn(response);

        authService.login(dto, session);

        verify(authFeignClient).login(dto);
        verify(storage).save(session, "jwt-token-123");
        verify(spanTagger).tag("auth.status", "success");
    }

    @Test
    void login_failure_shouldPropagateExceptionAndNotSaveToken() {
        var dto = new UserLoginRequestDto("user@example.com", "wrong");
        RuntimeException backendError = new RuntimeException("Backend error");

        when(authFeignClient.login(dto)).thenThrow(backendError);

        assertThatThrownBy(() -> authService.login(dto, session))
                .isInstanceOf(RuntimeException.class);

        verify(storage, org.mockito.Mockito.never()).save(session, "jwt-token-123");
    }

    @Test
    void logout_success_shouldClearSessionAndTag() {
        doNothing().when(storage).clear(session);

        authService.logout(session);

        verify(storage).clear(session);
        verify(spanTagger).tag(ObservationTags.AUTH_STATUS, "logout_success");
    }

    @Test
    void logout_failure_shouldPropagateException() {
        RuntimeException storageError = new RuntimeException("Storage error");
        doThrow(storageError).when(storage).clear(session);

        assertThatThrownBy(() -> authService.logout(session))
                .isInstanceOf(RuntimeException.class);
    }
}
