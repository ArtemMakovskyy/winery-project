package com.winestoreapp.wineryadminui.features.health;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "wine-store-service",
        url = "${api.backend-url}"
)
public interface WineHealthCheckFeignClient {

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    String healthCheck();
}
