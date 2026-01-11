package com.winestoreapp.order.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication(scanBasePackages = "com.winestoreapp.order")
@EnableMethodSecurity
public class OrderControllerTestConfig {
}
