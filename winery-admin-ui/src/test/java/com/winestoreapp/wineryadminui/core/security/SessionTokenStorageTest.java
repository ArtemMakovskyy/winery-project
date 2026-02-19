package com.winestoreapp.wineryadminui.core.security;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SessionTokenStorageTest {

    private SessionTokenStorage storage;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        storage = new SessionTokenStorage();
        session = mock(HttpSession.class);
    }

    @Test
    void save_shouldStoreTokenInSession() {
        String token = "dummy.token.value";

        storage.save(session, token);

        verify(session).setAttribute("TOKEN", token);
    }

    @Test
    void get_shouldReturnTokenFromSession() {
        when(session.getAttribute("TOKEN")).thenReturn("my-token");

        String token = storage.get(session);

        assertEquals("my-token", token);
    }

    @Test
    void getRoles_shouldReturnRolesFromSession() {
        List<String> roles = List.of("ROLE_ADMIN", "ROLE_USER");
        when(session.getAttribute("ROLES")).thenReturn(roles);

        List<String> result = storage.getRoles(session);

        assertEquals(roles, result);
    }

    @Test
    void clear_shouldInvalidateSession() {
        storage.clear(session);

        verify(session).invalidate();
    }

    @Test
    void save_shouldHandleInvalidTokenGracefully() {
        String badToken = "invalid.token";

        assertDoesNotThrow(() -> storage.save(session, badToken));

        verify(session).setAttribute("TOKEN", badToken);
    }
}
