package com.winestoreapp.common.config;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public class CustomMySqlContainer extends MySQLContainer<CustomMySqlContainer> {

    private static final DockerImageName IMAGE =
            DockerImageName.parse("mysql:8.0.36")
                    .asCompatibleSubstituteFor("mysql");

    private static CustomMySqlContainer container;

    private CustomMySqlContainer() {
        super(IMAGE);
        withDatabaseName("test");
        withUsername("test");
        withPassword("test");

        withCommand(
                "--default-authentication-plugin=mysql_native_password",
                "--character-set-server=utf8mb4",
                "--collation-server=utf8mb4_unicode_ci"
        );
    }

    public static synchronized CustomMySqlContainer getInstance() {
        if (container == null) {
            container = new CustomMySqlContainer();
        }
        return container;
    }
}