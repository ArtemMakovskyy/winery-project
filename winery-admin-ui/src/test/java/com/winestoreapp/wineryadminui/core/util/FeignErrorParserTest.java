package com.winestoreapp.wineryadminui.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import com.winestoreapp.wineryadminui.features.user.dto.BackendErrorResponse;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class FeignErrorParserTest {

    private ObjectMapper objectMapper;

    @Mock
    private SpanTagger spanTagger;

    private FeignErrorParser parser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        parser = new FeignErrorParser(objectMapper, spanTagger);
    }

    private FeignException createFeignException(int status, String body) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "http://localhost/test",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                null
        );

        Response response = Response.builder()
                .status(status)
                .request(request)
                .body(body, StandardCharsets.UTF_8)
                .build();

        return FeignException.errorStatus("test", response);
    }

    @Test
    void shouldExtractMessageFromValidBackendErrorJson() throws Exception {
        BackendErrorResponse backendError = new BackendErrorResponse(
                "2026-02-06T12:00:00",
                404,
                "Not Found",
                "Wine not found"
        );

        String json = objectMapper.writeValueAsString(backendError);
        FeignException exception = createFeignException(404, json);

        String result = parser.extractMessage(exception);

        assertThat(result).isEqualTo("Wine not found");
        verify(spanTagger).tag("feign.status", 404);
        verify(spanTagger).tag("backend.error.message", "Wine not found");
        verify(spanTagger, never()).event("error.parsing.failed");
    }

    @Test
    void shouldReturnFallbackMessageWhenBodyIsEmpty() {
        FeignException exception = createFeignException(500, "");

        String result = parser.extractMessage(exception);

        assertThat(result).isEqualTo("Service error: 500");
        verify(spanTagger).tag("feign.status", 500);
    }

    @Test
    void shouldReturnFallbackMessageWhenBodyIsInvalidJson() {
        String invalidJson = "{ this is not valid json }";
        FeignException exception = createFeignException(400, invalidJson);

        String result = parser.extractMessage(exception);

        assertThat(result).isEqualTo("Service error: 400");
        verify(spanTagger).tag("feign.status", 400);
        verify(spanTagger).event("error.parsing.failed");
    }
}
