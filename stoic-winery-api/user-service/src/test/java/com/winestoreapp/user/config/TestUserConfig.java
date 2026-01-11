package com.winestoreapp.user.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.winestoreapp.user")
@EnableJpaRepositories(basePackages = "com.winestoreapp.user.repository")
@EntityScan(basePackages = "com.winestoreapp.user.model")
public class TestUserConfig {
}