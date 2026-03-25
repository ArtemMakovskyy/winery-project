package com.winestoreapp.service;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
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

    /**
     * Get extended cache statistics with hit/miss rates and memory info.
     * Optimized to scan Redis only once and filter in memory.
     *
     * @return map with extended cache statistics
     */
    public Map<String, Object> getExtendedCacheStats() {
        log.debug("[REDIS-DEBUG] Getting extended cache statistics");
        Map<String, Object> stats = new LinkedHashMap<>();

        try {
            // Single SCAN to get all keys, then filter in memory
            Set<String> allKeys = getAllKeys();
            stats.put("totalKeys", allKeys.size());

            // Filter keys by pattern in memory (avoid multiple SCAN operations)
            Set<String> wineKeys = filterKeysByPattern(allKeys, "wines::*");
            Set<String> rateLimitKeys = filterKeysByPattern(allKeys, "rate_limit::*");
            Set<String> blacklistKeys = filterKeysByPattern(allKeys, "blacklist::*");

            stats.put("winesCacheKeys", wineKeys.size());
            stats.put("rateLimitKeys", rateLimitKeys.size());
            stats.put("blacklistKeys", blacklistKeys.size());

            // Memory info (from Redis INFO command)
            var connection = redisTemplate.getConnectionFactory().getConnection();
            try {
                var info = connection.info("memory");
                if (info != null) {
                    stats.put("usedMemory", info.get("used_memory"));
                    stats.put("usedMemoryHuman", info.get("used_memory_human"));
                    stats.put("usedMemoryPeak", info.get("used_memory_peak_human"));
                    stats.put("usedMemoryDataset", info.get("used_memory_dataset"));
                }

                var statsInfo = connection.info("stats");
                if (statsInfo != null) {
                    stats.put("keyspaceHits", statsInfo.get("keyspace_hits"));
                    stats.put("keyspaceMisses", statsInfo.get("keyspace_misses"));

                    Long hits = Long.parseLong(statsInfo.get("keyspace_hits").toString());
                    Long misses = Long.parseLong(statsInfo.get("keyspace_misses").toString());
                    Long total = hits + misses;
                    Double hitRate = total > 0 ? (hits.doubleValue() / total.doubleValue()) * 100 : 0.0;
                    stats.put("cacheHitRatePercent", String.format("%.2f", hitRate));
                }

                var serverInfo = connection.info("server");
                if (serverInfo != null) {
                    stats.put("redisVersion", serverInfo.get("redis_version"));
                    stats.put("uptimeInSeconds", serverInfo.get("uptime_in_seconds"));
                    stats.put("uptimeInDays", serverInfo.get("uptime_in_days"));
                }

                var clientsInfo = connection.info("clients");
                if (clientsInfo != null) {
                    stats.put("connectedClients", clientsInfo.get("connected_clients"));
                    stats.put("blockedClients", clientsInfo.get("blocked_clients"));
                }
            } finally {
                connection.close();
            }

            // Sample keys with details
            stats.put("sampleWineKeys", wineKeys.stream().limit(5).toList());
            stats.put("sampleRateLimitKeys", rateLimitKeys.stream().limit(5).toList());
            stats.put("sampleBlacklistKeys", blacklistKeys.stream().limit(5).toList());

            // Keys with TTL info (limit to first 10 wine keys)
            List<Map<String, Object>> wineKeysWithTtl = wineKeys.stream()
                    .limit(10)
                    .map(key -> {
                        Map<String, Object> keyInfo = new LinkedHashMap<>();
                        keyInfo.put("key", key);
                        Long ttl = getTTL(key);
                        // Handle TTL edge cases: -1 = no TTL, -2 = key doesn't exist
                        if (ttl == null || ttl < 0) {
                            keyInfo.put("ttlSeconds", ttl == -1 ? "no TTL" : "expired");
                        } else {
                            keyInfo.put("ttlSeconds", ttl);
                        }
                        return keyInfo;
                    })
                    .toList();
            stats.put("wineKeysWithTtl", wineKeysWithTtl);

        } catch (Exception e) {
            log.error("[REDIS-DEBUG] Error getting extended cache stats: {}", e.getMessage(), e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * Filter keys by pattern in memory (avoid multiple Redis SCAN operations).
     * Supports simple glob patterns: * (any chars), ? (single char)
     *
     * @param allKeys all keys from Redis
     * @param pattern pattern to filter by (e.g., "wines::*")
     * @return filtered set of keys
     */
    private Set<String> filterKeysByPattern(Set<String> allKeys, String pattern) {
        // Convert glob pattern to regex
        String regex = pattern
                .replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".");

        return allKeys.stream()
                .filter(key -> key.matches(regex))
                .collect(java.util.stream.Collectors.toSet());
    }
}
