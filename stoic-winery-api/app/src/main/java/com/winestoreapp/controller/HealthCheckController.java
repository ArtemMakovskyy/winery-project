package com.winestoreapp.controller;

import com.winestoreapp.common.ratelimit.RateLimit;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PATCH, RequestMethod.DELETE})
@Tag(name = "Health check management",
        description = "Endpoint health check controller")
public class HealthCheckController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Health check passed. "
                + "Application is running smoothly.");
    }

    @GetMapping("/rate-limited")
    @RateLimit(maxRequests = 5, timeWindowSeconds = 60)
    @Tag(name = "Rate Limit Test",
            description = "Test endpoint for rate limiting")
    public ResponseEntity<String> rateLimitedEndpoint() {
        return ResponseEntity.ok("Rate limit test passed! "
                + "You have remaining requests.");
    }

}
