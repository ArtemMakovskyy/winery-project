package com.winestoreapp.security.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class JwtUtilTest {
    private final String secret = "super-secret-key-that-is-at-least-32-characters-long";
    private final long expiration = 3600000L;
    private JwtUtil jwtUtil;
    private TokenBlacklistService blacklistService;

    @BeforeEach
    void setUp() {
        blacklistService = Mockito.mock(TokenBlacklistService.class);
        jwtUtil = new JwtUtil(secret, blacklistService);
        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);
    }

    @Test
    void shouldGenerateAndParseToken() {
        String email = "test@user.com";
        String token = jwtUtil.generateToken(email, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        Claims claims = jwtUtil.parseToken(token);

        assertEquals(email, claims.getSubject());
        assertEquals(List.of("ROLE_USER"), claims.get("roles"));
    }

    @Test
    void shouldThrowExceptionWhenTokenIsBlacklisted() {
        String token = jwtUtil.generateToken("user", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(blacklistService.isBlacklisted(token)).thenReturn(true);

        assertThrows(JwtException.class, () -> jwtUtil.parseToken(token));
    }

    @Test
    void shouldThrowExceptionOnInvalidToken() {
        String invalidToken = "invalid.token.string";

        assertThrows(JwtException.class, () -> jwtUtil.parseToken(invalidToken));
    }

    @Test
    void addToInvalidTokens_ShouldCallBlacklistService() {
        String token = jwtUtil.generateToken("user", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        doNothing().when(blacklistService).blacklist(any(), anyLong());

        jwtUtil.addToInvalidTokens(token);

        Mockito.verify(blacklistService).blacklist(any(), anyLong());
    }
}
