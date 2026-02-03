package com.winestoreapp.wineryadminui.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winestoreapp.wineryadminui.features.user.dto.BackendErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import feign.FeignException;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeignErrorParser {

    private final ObjectMapper objectMapper;

    public String extractMessage(FeignException e) {
        log.debug("Attempting to extract error message from FeignException. Status: {}", e.status());
        try {
            if (e.contentUTF8() != null && !e.contentUTF8().isBlank()) {
                BackendErrorResponse error = objectMapper.readValue(e.contentUTF8(), BackendErrorResponse.class);
                log.info("Successfully parsed backend error: {}", error.message());
                return error.message();
            }
        } catch (Exception ex) {
            log.error("Failed to parse backend error JSON. Raw content: {}", e.contentUTF8(), ex);
        }
        return "Service error: " + e.status();
    }
}