package com.winestoreapp.security.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication(scanBasePackages = {
        "com.winestoreapp.security",
        "com.winestoreapp.user"
})
@EnableMethodSecurity
public class AuthenticationControllerTestConfig {
}