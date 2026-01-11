package com.winestoreapp.security.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {
    private JwtUtil jwtUtil;
    private final String secret = "super-secret-key-that-is-at-least-32-characters-long";
    private final long expiration = 3600000L;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);
    }

    @Test
    void shouldGenerateAndParseToken() {
        String email = "test@user.com";
        String token = jwtUtil.generateToken(email);
        Claims claims = jwtUtil.parseToken(token);

        assertEquals(email, claims.getSubject());
    }

    @Test
    void shouldThrowExceptionWhenTokenIsBlacklisted() {
        String token = jwtUtil.generateToken("user");
        jwtUtil.addToInvalidTokens(token);

        assertThrows(JwtException.class, () -> jwtUtil.parseToken(token));
    }

    @Test
    void shouldThrowExceptionOnInvalidToken() {
        String invalidToken = "invalid.token.string";

        assertThrows(JwtException.class, () -> jwtUtil.parseToken(invalidToken));
    }
}
