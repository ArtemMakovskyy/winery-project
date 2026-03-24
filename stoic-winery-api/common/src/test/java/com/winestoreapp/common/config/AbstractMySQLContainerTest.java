package com.winestoreapp.common.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base test class for integration tests that require a MySQL database.
 * Uses Testcontainers to manage the database lifecycle.
 * <p>
 * Extend this class for your integration tests instead of manually
 * configuring the database connection.
 * <p>
 * Example usage:
 * <pre>{@code
 * @SpringBootTest(classes = TestConfig.class)
 * class MyIntegrationTest extends AbstractMySQLContainerTest {
 *
 *     @Test
 *     void testSomething() {
 *         // Your test code here
 *     }
 * }
 * }</pre>
 */
@Testcontainers
public abstract class AbstractMySQLContainerTest {

    protected static final CustomMySqlContainer mysqlContainer;

    static {
        // Start the container before any test runs
        mysqlContainer = CustomMySqlContainer.getInstance();
        mysqlContainer.start();
    }

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", mysqlContainer::getDriverClassName);
    }
}
