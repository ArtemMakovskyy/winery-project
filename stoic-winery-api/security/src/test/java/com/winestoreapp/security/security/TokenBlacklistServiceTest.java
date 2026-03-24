package com.winestoreapp.security.security;

import com.winestoreapp.security.config.RedisBlacklistProperties;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RedisBlacklistProperties properties;

    private TokenBlacklistService blacklistService;

    @BeforeEach
    void setUp() {
        lenient().when(properties.getPrefix()).thenReturn("blacklist:");
        lenient().when(properties.getDefaultTtl()).thenReturn(3600L);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        blacklistService = new TokenBlacklistService(redisTemplate, properties);
    }

    @Test
    void blacklist_ShouldSetTokenWithTtl() {
        String token = "test-jwt-token";
        long ttl = 3600L;

        blacklistService.blacklist(token, ttl);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);

        verify(valueOperations).set(
                keyCaptor.capture(),
                eq("revoked"),
                ttlCaptor.capture(),
                eq(TimeUnit.SECONDS)
        );

        assertEquals("blacklist:test-jwt-token", keyCaptor.getValue());
        assertEquals(ttl, ttlCaptor.getValue());
    }

    @Test
    void isBlacklisted_WhenTokenExists_ShouldReturnTrue() {
        String token = "test-jwt-token";
        when(redisTemplate.hasKey("blacklist:" + token)).thenReturn(true);

        boolean result = blacklistService.isBlacklisted(token);

        assertTrue(result);
        verify(redisTemplate).hasKey("blacklist:" + token);
    }

    @Test
    void isBlacklisted_WhenTokenDoesNotExist_ShouldReturnFalse() {
        String token = "test-jwt-token";
        when(redisTemplate.hasKey("blacklist:" + token)).thenReturn(false);

        boolean result = blacklistService.isBlacklisted(token);

        assertFalse(result);
        verify(redisTemplate).hasKey("blacklist:" + token);
    }

    @Test
    void removeFromBlacklist_ShouldDeleteToken() {
        String token = "test-jwt-token";

        blacklistService.removeFromBlacklist(token);

        verify(redisTemplate).delete("blacklist:" + token);
    }
}
