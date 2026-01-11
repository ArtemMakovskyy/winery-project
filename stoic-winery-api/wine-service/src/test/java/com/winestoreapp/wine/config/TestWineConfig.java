package com.winestoreapp.wine.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.winestoreapp.wine")
@EnableJpaRepositories(basePackages = "com.winestoreapp.wine.repository")
@EntityScan(basePackages = "com.winestoreapp.wine.model")
public class TestWineConfig {
}