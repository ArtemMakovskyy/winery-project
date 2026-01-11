package com.winestoreapp.order.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.winestoreapp.order")
@EnableJpaRepositories(basePackages = "com.winestoreapp.order.repository")
@EntityScan(basePackages = "com.winestoreapp.order.model")
public class TestServiceConfig {
}