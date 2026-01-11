package com.winestoreapp.review.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication(scanBasePackages = "com.winestoreapp.review")
@EnableMethodSecurity
public class ReviewControllerTestConfig {
}
