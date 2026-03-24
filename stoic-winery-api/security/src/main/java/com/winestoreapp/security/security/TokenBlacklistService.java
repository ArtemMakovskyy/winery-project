package com.winestoreapp.security.security;

import com.winestoreapp.security.config.RedisBlacklistProperties;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for managing JWT token blacklist using Redis.
 * Stores revoked tokens with TTL to prevent reuse after logout.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private final RedisBlacklistProperties properties;

    /**
     * Adds a token to the blacklist with TTL.
     *
     * @param token      the JWT token to blacklist
     * @param ttlSeconds time to live in seconds (should match JWT expiration)
     */
    public void blacklist(String token, long ttlSeconds) {
        String key = properties.getPrefix() + token;

        redisTemplate.opsForValue().set(key, "revoked", ttlSeconds, TimeUnit.SECONDS);

        log.debug("Token blacklisted: key={}, ttl={}s", key, ttlSeconds);
    }

    /**
     * Checks if a token is blacklisted.
     *
     * @param token the JWT token to check
     * @return true if token is blacklisted, false otherwise
     */
    public boolean isBlacklisted(String token) {
        String key = properties.getPrefix() + token;

        Boolean exists = redisTemplate.hasKey(key);

        return Boolean.TRUE.equals(exists);
    }

    /**
     * Removes a token from the blacklist (optional operation).
     *
     * @param token the JWT token to remove from blacklist
     */
    public void removeFromBlacklist(String token) {
        String key = properties.getPrefix() + token;

        redisTemplate.delete(key);

        log.debug("Token removed from blacklist: key={}", key);
    }
}
