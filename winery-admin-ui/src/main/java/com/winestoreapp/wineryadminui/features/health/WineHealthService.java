// WineHealthService.java
package com.winestoreapp.wineryadminui.features.health;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import io.micrometer.tracing.Tracer;
import io.micrometer.observation.annotation.Observed;

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
        var newSpan = tracer.nextSpan().name("health.check.startup").start();
        try (var ignored = tracer.withSpan(newSpan)) {
            String healthCheck = wineHealthCheckFeignClient.healthCheck();
            newSpan.tag("status", "success");
            log.info("Connection to WINE-STORE-SERVICE successful. Response: {}", healthCheck);
        } catch (Exception e) {
            newSpan.tag("status", "error");
            newSpan.error(e);
            log.error("CRITICAL: Failed to connect to WINE-STORE-SERVICE at startup. Error: {}", e.getMessage());
        } finally {
            newSpan.end();
        }
    }
}
