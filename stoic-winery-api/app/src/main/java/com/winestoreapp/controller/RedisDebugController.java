package com.winestoreapp.controller;

import com.winestoreapp.service.RedisDebugService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for Redis debugging and inspection.
 * Allows viewing and managing Redis cache entries.
 */
@RestController
@RequestMapping("/redis")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Redis Debug", description = "Endpoints for Redis cache inspection and debugging")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
public class RedisDebugController {

    private final RedisDebugService redisDebugService;

    /**
     * Get all Redis keys.
     */
    @GetMapping("/keys")
    @Operation(summary = "Get all Redis keys", description = "Returns all keys in Redis (Admin only)")
    public ResponseEntity<Set<String>> getAllKeys() {
        log.info("[REDIS-DEBUG] Request to get all Redis keys");
        return ResponseEntity.ok(redisDebugService.getAllKeys());
    }

    /**
     * Get keys matching a pattern.
     */
    @GetMapping("/keys/pattern")
    @Operation(summary = "Get keys by pattern", description = "Returns keys matching the specified pattern")
    public ResponseEntity<Set<String>> getKeysByPattern(
            @RequestParam("pattern") String pattern
    ) {
        log.info("[REDIS-DEBUG] Request to get keys with pattern: {}", pattern);
        return ResponseEntity.ok(redisDebugService.getKeys(pattern));
    }

    /**
     * Get value by key.
     */
    @GetMapping("/value")
    @Operation(summary = "Get value by key", description = "Returns the value stored at the specified Redis key")
    public ResponseEntity<Object> getValue(
            @RequestParam("key") String key
    ) {
        log.info("[REDIS-DEBUG] Request to get value for key: {}", key);
        Object value = redisDebugService.getValue(key);
        return ResponseEntity.ok(value != null ? value : "Key not found");
    }

    /**
     * Get detailed key information.
     */
    @GetMapping("/key-info")
    @Operation(summary = "Get key info", description = "Returns detailed information about a Redis key (value, TTL, type)")
    public ResponseEntity<Map<String, Object>> getKeyInfo(
            @RequestParam("key") String key
    ) {
        log.info("[REDIS-DEBUG] Request to get info for key: {}", key);
        return ResponseEntity.ok(redisDebugService.getKeyInfo(key));
    }

    /**
     * Get TTL for a key.
     */
    @GetMapping("/ttl")
    @Operation(summary = "Get TTL", description = "Returns the TTL (time to live) in seconds for a Redis key")
    public ResponseEntity<Long> getTTL(
            @RequestParam("key") String key
    ) {
        log.info("[REDIS-DEBUG] Request to get TTL for key: {}", key);
        return ResponseEntity.ok(redisDebugService.getTTL(key));
    }

    /**
     * Get cache statistics.
     */
    @GetMapping("/stats")
    @Operation(summary = "Get cache statistics", description = "Returns statistics about Redis cache (key counts, sample keys)")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        log.info("[REDIS-DEBUG] Request to get cache statistics");
        return ResponseEntity.ok(redisDebugService.getCacheStats());
    }

    /**
     * Get wine cache keys.
     */
    @GetMapping("/keys/wines")
    @Operation(summary = "Get wine cache keys", description = "Returns all wine cache keys (wines::*)")
    public ResponseEntity<Set<String>> getWineCacheKeys() {
        log.info("[REDIS-DEBUG] Request to get wine cache keys");
        return ResponseEntity.ok(redisDebugService.getKeys("wines::*"));
    }

    /**
     * Get rate limit keys.
     */
    @GetMapping("/keys/rate-limits")
    @Operation(summary = "Get rate limit keys", description = "Returns all rate limit keys (rate_limit::*)")
    public ResponseEntity<Set<String>> getRateLimitKeys() {
        log.info("[REDIS-DEBUG] Request to get rate limit keys");
        return ResponseEntity.ok(redisDebugService.getKeys("rate_limit::*"));
    }

    /**
     * Get blacklist keys.
     */
    @GetMapping("/keys/blacklist")
    @Operation(summary = "Get blacklist keys", description = "Returns all JWT blacklist keys (blacklist::*)")
    public ResponseEntity<Set<String>> getBlacklistKeys() {
        log.info("[REDIS-DEBUG] Request to get blacklist keys");
        return ResponseEntity.ok(redisDebugService.getKeys("blacklist::*"));
    }

    /**
     * Delete a specific key.
     */
    @DeleteMapping("/key")
    @Operation(summary = "Delete key", description = "Deletes a specific Redis key")
    public ResponseEntity<Map<String, Object>> deleteKey(
            @RequestParam("key") String key
    ) {
        log.info("[REDIS-DEBUG] Request to delete key: {}", key);
        boolean deleted = redisDebugService.deleteKey(key);

        Map<String, Object> response = Map.of(
                "key", key,
                "deleted", deleted
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Clear wine cache.
     */
    @DeleteMapping("/cache/wines")
    @Operation(summary = "Clear wine cache", description = "Deletes all wine cache entries (wines::*)")
    public ResponseEntity<Map<String, Object>> clearWineCache() {
        log.info("[REDIS-DEBUG] Request to clear wine cache");
        redisDebugService.clearWinesCache();

        Map<String, Object> response = Map.of(
                "message", "Wine cache cleared",
                "pattern", "wines::*"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Clear rate limit cache.
     */
    @DeleteMapping("/cache/rate-limits")
    @Operation(summary = "Clear rate limit cache", description = "Deletes all rate limit entries (rate_limit::*)")
    public ResponseEntity<Map<String, Object>> clearRateLimitCache() {
        log.info("[REDIS-DEBUG] Request to clear rate limit cache");
        redisDebugService.clearRateLimitCache();

        Map<String, Object> response = Map.of(
                "message", "Rate limit cache cleared",
                "pattern", "rate_limit::*"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get sample wine cache entries with values.
     */
    @GetMapping("/cache/wines/sample")
    @Operation(summary = "Get sample wine cache", description = "Returns up to 5 wine cache entries with their values")
    public ResponseEntity<List<Map<String, Object>>> getSampleWineCache() {
        log.info("[REDIS-DEBUG] Request to get sample wine cache entries");

        Set<String> wineKeys = redisDebugService.getKeys("wines::*");
        List<Map<String, Object>> samples = wineKeys.stream()
                .limit(5)
                .map(redisDebugService::getKeyInfo)
                .toList();

        return ResponseEntity.ok(samples);
    }

    /**
     * Get sample rate limit entries with values.
     */
    @GetMapping("/cache/rate-limits/sample")
    @Operation(summary = "Get sample rate limit cache", description = "Returns up to 5 rate limit entries with their values and TTL")
    public ResponseEntity<List<Map<String, Object>>> getSampleRateLimitCache() {
        log.info("[REDIS-DEBUG] Request to get sample rate limit entries");

        Set<String> rateLimitKeys = redisDebugService.getKeys("rate_limit::*");
        List<Map<String, Object>> samples = rateLimitKeys.stream()
                .limit(5)
                .map(redisDebugService::getKeyInfo)
                .toList();

        return ResponseEntity.ok(samples);
    }

    /**
     * Get extended cache statistics.
     */
    @GetMapping("/stats/extended")
    @Operation(summary = "Get extended cache statistics", description = "Returns detailed statistics about Redis cache including memory, hit/miss rates, uptime, and key details")
    public ResponseEntity<Map<String, Object>> getExtendedCacheStats() {
        log.info("[REDIS-DEBUG] Request to get extended cache statistics");
        return ResponseEntity.ok(redisDebugService.getExtendedCacheStats());
    }
}
