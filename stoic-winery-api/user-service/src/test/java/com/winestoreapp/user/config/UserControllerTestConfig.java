package com.winestoreapp.user.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication(scanBasePackages = "com.winestoreapp.user")
@EnableMethodSecurity
public class UserControllerTestConfig {
}
