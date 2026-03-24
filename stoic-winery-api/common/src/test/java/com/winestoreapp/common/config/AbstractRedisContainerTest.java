package com.winestoreapp.common.config;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base test class for integration tests that require a Redis instance.
 * Uses Testcontainers to manage the Redis lifecycle.
 * <p>
 * Extend this class for your integration tests that need Redis.
 * <p>
 * Example usage:
 * <pre>{@code
 * @SpringBootTest(classes = TestConfig.class)
 * class MyRedisIntegrationTest extends AbstractRedisContainerTest {
 *
 *     @Autowired
 *     private StringRedisTemplate redisTemplate;
 *
 *     @Test
 *     void testRedisOperations() {
 *         // Your test code here
 *     }
 * }
 * }</pre>
 */
@Testcontainers
public abstract class AbstractRedisContainerTest {

    protected static final GenericContainer<?> redisContainer =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    static {
        redisContainer.start();
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    /**
     * Creates a RedisConnectionFactory using the test container's host and port.
     * Useful for manual Redis configuration in tests.
     */
    protected RedisConnectionFactory createRedisConnectionFactory() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(
                redisContainer.getHost(),
                redisContainer.getMappedPort(6379)
        );
        factory.afterPropertiesSet();
        return factory;
    }
}
