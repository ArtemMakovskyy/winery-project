package com.winestoreapp.wineryadminui.features.health;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WineHealthService {

    private final WineHealthCheckFeignClient wineHealthCheckFeignClient;

    @PostConstruct
    public void init() {
        log.info("Initializing WineHealthService health check...");
        check();
    }

    public void check() {
        try {
            String healthCheck = wineHealthCheckFeignClient.healthCheck();
            log.info("Connection to WINE-STORE-SERVICE successful. Response: {}", healthCheck);
        } catch (Exception e) {
            log.error("CRITICAL: Failed to connect to WINE-STORE-SERVICE at startup. Error: {}", e.getMessage());
        }
    }
}
