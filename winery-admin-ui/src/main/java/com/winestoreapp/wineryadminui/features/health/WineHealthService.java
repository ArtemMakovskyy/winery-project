package com.winestoreapp.wineryadminui.features.health;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class WineHealthService {

    private final WineHealthCheckFeignClient wineHealthCheckFeignClient;

    @PostConstruct
    public void init() {
        check();
    }

    public void check() {
        try {
            String healthCheck = wineHealthCheckFeignClient.healthCheck();
            log.info("Connection to WINE-STORE-SERVICE successful: {}", healthCheck);
        } catch (Exception e) {
            log.error("Failed to connect to WINE-STORE-SERVICE: {}", e.getMessage());
        }
    }
}
