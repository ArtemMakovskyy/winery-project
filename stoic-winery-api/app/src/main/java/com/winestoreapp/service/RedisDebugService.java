package com.winestoreapp.service;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

/**
 * Service for debugging and inspecting Redis data.
 * Provides methods to view keys, values, and TTL information.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RedisDebugService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Get all keys matching a pattern.
     *
     * @param pattern key pattern (e.g., "wines::*", "rate_limit::*")
     * @return set of matching keys
     */
    public Set<String> getKeys(String pattern) {
        log.debug("[REDIS-DEBUG] Getting keys with pattern: {}", pattern);
        Set<String> keys = new HashSet<>();
        
        try {
            ScanOptions scanOptions = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(1000)
                    .build();
            
            try (var cursor = redisTemplate.scan(scanOptions)) {
                while (cursor.hasNext()) {
                    keys.add(cursor.next());
                }
            }
            
            log.debug("[REDIS-DEBUG] Found {} keys matching pattern '{}'", keys.size(), pattern);
        } catch (Exception e) {
            log.error("[REDIS-DEBUG] Error scanning keys with pattern '{}': {}", pattern, e.getMessage(), e);
        }
        
        return keys;
    }

    /**
     * Get all keys in Redis (use with caution in production).
     *
     * @return set of all keys
     */
    public Set<String> getAllKeys() {
        return getKeys("*");
    }

    /**
     * Get value by key.
     *
     * @param key the Redis key
     * @return the value stored at key, or null if key doesn't exist
     */
    public Object getValue(String key) {
        log.debug("[REDIS-DEBUG] Getting value for key: {}", key);
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("[REDIS-DEBUG] Value for key '{}': {}", key, value != null ? value.getClass().getSimpleName() : "null");
            return value;
        } catch (Exception e) {
            log.error("[REDIS-DEBUG] Error getting value for key '{}': {}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get TTL (time to live) for a key in seconds.
     *
     * @param key the Redis key
     * @return TTL in seconds, -1 if no TTL, -2 if key doesn't exist
     */
    public Long getTTL(String key) {
        log.debug("[REDIS-DEBUG] Getting TTL for key: {}", key);
        try {
            Long ttl = redisTemplate.getExpire(key);
            log.debug("[REDIS-DEBUG] TTL for key '{}': {} seconds", key, ttl);
            return ttl;
        } catch (Exception e) {
            log.error("[REDIS-DEBUG] Error getting TTL for key '{}': {}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get detailed information about a key.
     *
     * @param key the Redis key
     * @return map with key info (value, ttl, type)
     */
    public Map<String, Object> getKeyInfo(String key) {
        log.debug("[REDIS-DEBUG] Getting info for key: {}", key);
        Map<String, Object> info = new LinkedHashMap<>();
        
        try {
            info.put("key", key);
            info.put("value", getValue(key));
            info.put("ttlSeconds", getTTL(key));
            info.put("type", getType(key));
        } catch (Exception e) {
            log.error("[REDIS-DEBUG] Error getting info for key '{}': {}", key, e.getMessage(), e);
            info.put("error", e.getMessage());
        }
        
        return info;
    }

    /**
     * Get the Redis type of a key.
     *
     * @param key the Redis key
     * @return the type as string (STRING, LIST, SET, ZSET, HASH, NONE)
     */
    public String getType(String key) {
        try {
            return redisTemplate.type(key).code();
        } catch (Exception e) {
            log.error("[REDIS-DEBUG] Error getting type for key '{}': {}", key, e.getMessage(), e);
            return "ERROR";
        }
    }

    /**
     * Delete a key from Redis.
     *
     * @param key the key to delete
     * @return true if key was deleted, false otherwise
     */
    public boolean deleteKey(String key) {
        log.debug("[REDIS-DEBUG] Deleting key: {}", key);
        try {
            Boolean deleted = redisTemplate.delete(key);
            log.debug("[REDIS-DEBUG] Key '{}' deleted: {}", key, deleted);
            return Boolean.TRUE.equals(deleted);
        } catch (Exception e) {
            log.error("[REDIS-DEBUG] Error deleting key '{}': {}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Delete all keys matching a pattern.
     *
     * @param pattern key pattern to delete
     * @return number of keys deleted
     */
    public long deleteKeysByPattern(String pattern) {
        log.debug("[REDIS-DEBUG] Deleting keys with pattern: {}", pattern);
        Set<String> keys = getKeys(pattern);
        long count = 0;
        
        for (String key : keys) {
            if (deleteKey(key)) {
                count++;
            }
        }
        
        log.debug("[REDIS-DEBUG] Deleted {} keys matching pattern '{}'", count, pattern);
        return count;
    }

    /**
     * Get cache statistics.
     *
     * @return map with cache statistics
     */
    public Map<String, Object> getCacheStats() {
        log.debug("[REDIS-DEBUG] Getting cache statistics");
        Map<String, Object> stats = new LinkedHashMap<>();
        
        try {
            Set<String> allKeys = getAllKeys();
            stats.put("totalKeys", allKeys.size());
            
            // Count keys by pattern
            stats.put("winesCacheKeys", getKeys("wines::*").size());
            stats.put("rateLimitKeys", getKeys("rate_limit::*").size());
            stats.put("blacklistKeys", getKeys("blacklist::*").size());
            
            // Sample keys
            stats.put("sampleWineKeys", getKeys("wines::*").stream().limit(5).toList());
            stats.put("sampleRateLimitKeys", getKeys("rate_limit::*").stream().limit(5).toList());
            
        } catch (Exception e) {
            log.error("[REDIS-DEBUG] Error getting cache stats: {}", e.getMessage(), e);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }

    /**
     * Clear all cache entries (wines::* pattern).
     */
    public void clearWinesCache() {
        log.info("[REDIS-DEBUG] Clearing wines cache");
        long deleted = deleteKeysByPattern("wines::*");
        log.info("[REDIS-DEBUG] Cleared {} wine cache entries", deleted);
    }

    /**
     * Clear all rate limit entries.
     */
    public void clearRateLimitCache() {
        log.info("[REDIS-DEBUG] Clearing rate limit cache");
        long deleted = deleteKeysByPattern("rate_limit::*");
        log.info("[REDIS-DEBUG] Cleared {} rate limit entries", deleted);
    }
}
