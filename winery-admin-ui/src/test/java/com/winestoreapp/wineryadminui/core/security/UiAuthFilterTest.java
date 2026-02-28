package com.winestoreapp.wineryadminui.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UiAuthFilterTest {

    @Mock
    private SessionTokenStorage storage;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private HttpSession session;

    @InjectMocks
    private UiAuthFilter filter;

    @ParameterizedTest
    @ValueSource(strings = {"/login", "/css/style.css", "/api/health", "/favicon.ico", "/actuator/info"})
    void publicAndNonUiPaths_ShouldPassThrough(String path) throws Exception {
        when(request.getRequestURI()).thenReturn(path);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(storage);
    }

    @Test
    void uiPath_WithoutAuthenticatedSession_ShouldRedirectToLogin() throws Exception {
        when(request.getRequestURI()).thenReturn("/ui/dashboard");
        when(request.getSession(false)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).sendRedirect("/login");
        verifyNoInteractions(filterChain);
    }

    @Test
    void adminPath_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        when(request.getRequestURI()).thenReturn("/ui/users/create");
        when(request.getSession(false)).thenReturn(session);
        when(storage.get(session)).thenReturn("valid-token");
        when(storage.getRoles(session)).thenReturn(List.of("ROLE_USER"));

        filter.doFilterInternal(request, response, filterChain);

        // Сообщение синхронизировано с реализацией фильтра
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required");
        verifyNoInteractions(filterChain);
    }

    @Test
    void managerPath_WithManagerRole_ShouldAllowAccess() throws Exception {
        when(request.getRequestURI()).thenReturn("/ui/wines/edit/1");
        when(request.getSession(false)).thenReturn(session);
        when(storage.get(session)).thenReturn("token");
        when(storage.getRoles(session)).thenReturn(List.of("ROLE_MANAGER"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
