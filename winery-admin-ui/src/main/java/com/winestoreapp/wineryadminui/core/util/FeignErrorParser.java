package com.winestoreapp.wineryadminui.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import com.winestoreapp.wineryadminui.features.user.dto.BackendErrorResponse;
import feign.FeignException;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeignErrorParser {

    private final ObjectMapper objectMapper;
    private final SpanTagger spanTagger;

    @Observed(name = "feign.error.parser")
    public String extractMessage(FeignException e) {
        log.debug("Attempting to extract error message from FeignException. Status: {}", e.status());

        spanTagger.tag("feign.status", e.status());

        try {
            if (e.contentUTF8() != null && !e.contentUTF8().isBlank()) {
                BackendErrorResponse error = objectMapper.readValue(e.contentUTF8(), BackendErrorResponse.class);
                log.info("Successfully parsed backend error: {}", error.message());

                spanTagger.tag("backend.error.message", error.message());

                return error.message();
            }
        } catch (Exception ex) {
            log.error("Failed to parse backend error JSON. Raw content: {}", e.contentUTF8(), ex);
            spanTagger.event("error.parsing.failed");
        }
        return "Service error: " + e.status();
    }

}
