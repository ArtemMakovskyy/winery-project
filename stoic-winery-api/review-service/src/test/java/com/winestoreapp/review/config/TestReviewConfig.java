package com.winestoreapp.review.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.winestoreapp.review")
@EnableJpaRepositories(basePackages = "com.winestoreapp.review.repository")
@EntityScan(basePackages = "com.winestoreapp.review.model")
public class TestReviewConfig {
}