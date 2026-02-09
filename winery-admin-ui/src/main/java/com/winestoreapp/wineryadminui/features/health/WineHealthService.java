package com.winestoreapp.wineryadminui.features.health;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;

@Service
@RequiredArgsConstructor
@Slf4j
public class WineHealthService {

    private final WineHealthCheckFeignClient wineHealthCheckFeignClient;
    private final Tracer tracer;

    @PostConstruct
    @Observed(name = "health.service.init")
    public void init() {
        log.info("Initializing WineHealthService health check...");
        check();
    }

    @Observed(name = "health.service.check")
    public void check() {
        try {
            String healthCheck = wineHealthCheckFeignClient.healthCheck();
            tagSpan("health.status", "UP");
            tagSpan("status", "success");
            log.info("Connection to WINE-STORE-SERVICE successful. Response: {}", healthCheck);
        } catch (Exception e) {
            tagSpan("status", "error");
            recordError(e);
            log.error("CRITICAL: Failed to connect to WINE-STORE-SERVICE. Error: {}", e.getMessage());
        }
    }

    private void tagSpan(String key, Object value) {
        var span = tracer.currentSpan();
        if (span != null) {
            span.tag(key, String.valueOf(value));
        }
    }

    private void recordError(Throwable e) {
        var span = tracer.currentSpan();
        if (span != null) {
            span.error(e);
        }
    }
}