package com.winestoreapp.common.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for RateLimitService with Redis.
 * Tests rate limiting functionality using Lua script.
 */
@Testcontainers
class RateLimitServiceIntegrationTest {

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    private RateLimitServiceImpl rateLimitService;
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        // Create Redis connection
        LettuceConnectionFactory factory = new LettuceConnectionFactory(
                redisContainer.getHost(),
                redisContainer.getMappedPort(6379)
        );
        factory.afterPropertiesSet();

        redisTemplate = new StringRedisTemplate(factory);
        rateLimitService = new RateLimitServiceImpl(redisTemplate);

        // Clear Redis before each test
        RedisConnection connection = factory.getConnection();
        connection.serverCommands().flushAll();
        connection.close();
    }

    // ==================== Basic Rate Limit Tests ====================

    @Test
    void check_FirstRequest_ShouldBeAllowed() {
        RateLimitResult result = rateLimitService.check("test-key", 5, 60);

        assertThat(result.allowed()).isTrue();
        assertThat(result.currentCount()).isEqualTo(1);
    }

    @Test
    void check_WithinLimit_ShouldBeAllowed() {
        String key = "test-within-limit";
        int maxRequests = 5;
        int windowSeconds = 60;

        // Make 3 requests
        RateLimitResult result1 = rateLimitService.check(key, maxRequests, windowSeconds);
        RateLimitResult result2 = rateLimitService.check(key, maxRequests, windowSeconds);
        RateLimitResult result3 = rateLimitService.check(key, maxRequests, windowSeconds);

        assertThat(result1.allowed()).isTrue();
        assertThat(result1.currentCount()).isEqualTo(1);

        assertThat(result2.allowed()).isTrue();
        assertThat(result2.currentCount()).isEqualTo(2);

        assertThat(result3.allowed()).isTrue();
        assertThat(result3.currentCount()).isEqualTo(3);
    }

    @Test
    void check_ExceedLimit_ShouldBeDenied() {
        String key = "test-exceed-limit";
        int maxRequests = 3;
        int windowSeconds = 60;

        // Make requests until limit is exceeded
        rateLimitService.check(key, maxRequests, windowSeconds); // 1
        rateLimitService.check(key, maxRequests, windowSeconds); // 2
        rateLimitService.check(key, maxRequests, windowSeconds); // 3
        RateLimitResult result4 = rateLimitService.check(key, maxRequests, windowSeconds); // 4 - exceeded

        assertThat(result4.allowed()).isFalse();
    }

    @Test
    void check_MultipleKeys_ShouldBeIndependent() {
        int maxRequests = 2;
        int windowSeconds = 60;

        // Key 1: 2 requests
        rateLimitService.check("key-1", maxRequests, windowSeconds);
        rateLimitService.check("key-1", maxRequests, windowSeconds);
        RateLimitResult key1Result = rateLimitService.check("key-1", maxRequests, windowSeconds);

        // Key 2: 1 request
        RateLimitResult key2Result = rateLimitService.check("key-2", maxRequests, windowSeconds);

        assertThat(key1Result.allowed()).isFalse(); // key-1 exceeded
        assertThat(key2Result.allowed()).isTrue();  // key-2 still allowed
        assertThat(key2Result.currentCount()).isEqualTo(1);
    }

    // ==================== Redis Key Tests ====================

    @Test
    void check_RedisKey_ShouldHaveCorrectPrefix() {
        String key = "test-redis-prefix";
        rateLimitService.check(key, 5, 60);

        Boolean exists = redisTemplate.hasKey("rate_limit:" + key);
        assertThat(exists).isTrue();
    }

    @Test
    void check_RedisKey_ShouldHaveTTL() {
        String key = "test-ttl";
        int windowSeconds = 60;

        rateLimitService.check(key, 5, windowSeconds);

        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        Long ttl = connection.keyCommands().ttl(("rate_limit:" + key).getBytes());
        connection.close();

        assertThat(ttl).isGreaterThan(0);
        assertThat(ttl).isLessThanOrEqualTo(windowSeconds);
    }

    // ==================== Edge Cases ====================

    @Test
    void check_ZeroMaxRequests_ShouldBeDenied() {
        // Note: With maxRequests=0, the first request increments to 1, which is > 0
        // So the second request should be denied
        String key = "zero-limit";

        // First request increments counter to 1
        rateLimitService.check(key, 0, 60);

        // Second request should be denied (1 > 0)
        RateLimitResult result = rateLimitService.check(key, 0, 60);

        assertThat(result.allowed()).isFalse();
    }

    @Test
    void check_SpecialCharactersInKey_ShouldWork() {
        String key = "test-key:with@special#chars";

        RateLimitResult result = rateLimitService.check(key, 5, 60);

        assertThat(result.allowed()).isTrue();

        Boolean exists = redisTemplate.hasKey("rate_limit:" + key);
        assertThat(exists).isTrue();
    }

    // ==================== Reset Tests ====================

    @Test
    void reset_ShouldClearRateLimit() {
        String key = "test-reset";
        int maxRequests = 2;

        // Exceed limit
        rateLimitService.check(key, maxRequests, 60);
        rateLimitService.check(key, maxRequests, 60);
        RateLimitResult result1 = rateLimitService.check(key, maxRequests, 60);
        assertThat(result1.allowed()).isFalse();

        // Reset
        rateLimitService.reset(key);

        // Should be allowed again
        RateLimitResult result2 = rateLimitService.check(key, maxRequests, 60);
        assertThat(result2.allowed()).isTrue();
        assertThat(result2.currentCount()).isEqualTo(1);
    }
}
