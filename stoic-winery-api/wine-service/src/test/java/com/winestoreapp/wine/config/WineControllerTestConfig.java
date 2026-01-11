package com.winestoreapp.wine.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication(scanBasePackages = "com.winestoreapp.wine")
@EnableMethodSecurity
public class WineControllerTestConfig {
}
