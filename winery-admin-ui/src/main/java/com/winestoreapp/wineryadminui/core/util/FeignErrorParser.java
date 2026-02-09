package com.winestoreapp.wineryadminui.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winestoreapp.wineryadminui.features.user.dto.BackendErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import feign.FeignException;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeignErrorParser {

    private final ObjectMapper objectMapper;
    private final Tracer tracer;

    @Observed(name = "feign.error.parser")
    public String extractMessage(FeignException e) {
        log.debug("Attempting to extract error message from FeignException. Status: {}", e.status());

        tagSpan("feign.status", e.status());

        try {
            if (e.contentUTF8() != null && !e.contentUTF8().isBlank()) {
                BackendErrorResponse error = objectMapper.readValue(e.contentUTF8(), BackendErrorResponse.class);
                log.info("Successfully parsed backend error: {}", error.message());

                tagSpan("backend.error.message", error.message());

                return error.message();
            }
        } catch (Exception ex) {
            log.error("Failed to parse backend error JSON. Raw content: {}", e.contentUTF8(), ex);
            addEvent("error.parsing.failed");
        }
        return "Service error: " + e.status();
    }

    private void tagSpan(String key, Object value) {
        if (tracer.currentSpan() != null) {
            tracer.currentSpan().tag(key, String.valueOf(value));
        }
    }

    private void addEvent(String eventName) {
        if (tracer.currentSpan() != null) {
            tracer.currentSpan().event(eventName);
        }
    }
}
