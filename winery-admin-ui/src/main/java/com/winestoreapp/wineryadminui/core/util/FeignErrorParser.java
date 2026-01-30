package com.winestoreapp.wineryadminui.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winestoreapp.wineryadminui.features.user.dto.BackendErrorResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeignErrorParser {

    private final ObjectMapper objectMapper;

    public String extractMessage(FeignException e) {
        try {
            if (e.contentUTF8() != null && !e.contentUTF8().isBlank()) {
                BackendErrorResponse error = objectMapper.readValue(e.contentUTF8(), BackendErrorResponse.class);
                return error.message();
            }
        } catch (Exception ex) {
            log.error("Failed to parse backend error JSON", ex);
        }
        return "Service error: " + e.status();
    }
}
