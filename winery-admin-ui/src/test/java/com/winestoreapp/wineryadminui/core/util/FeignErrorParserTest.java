package com.winestoreapp.wineryadminui.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winestoreapp.wineryadminui.features.user.dto.BackendErrorResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import feign.FeignException;
import feign.Request;
import feign.Response;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeignErrorParserTest {

    private ObjectMapper objectMapper;
    private Tracer tracer;
    private FeignErrorParser parser;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        tracer = mock(Tracer.class);
        parser = new FeignErrorParser(objectMapper, tracer);
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
        // given
        BackendErrorResponse backendError = new BackendErrorResponse(
                "2026-02-06T12:00:00",
                404,
                "Not Found",
                "Wine not found"
        );

        String json = objectMapper.writeValueAsString(backendError);
        FeignException exception = createFeignException(404, json);

        Span span = mock(Span.class);
        when(tracer.currentSpan()).thenReturn(span);

        // when
        String result = parser.extractMessage(exception);

        // then
        assertThat(result).isEqualTo("Wine not found");
        verify(span).tag("feign.status", "404");
        verify(span, never()).event("error.parsing.failed");
    }

    @Test
    void shouldReturnFallbackMessageWhenBodyIsEmpty() {
        // given
        FeignException exception = createFeignException(500, "");

        // when
        String result = parser.extractMessage(exception);

        // then
        assertThat(result).isEqualTo("Service error: 500");
    }

    @Test
    void shouldReturnFallbackMessageWhenBodyIsInvalidJson() {
        // given
        String invalidJson = "{ this is not valid json }";
        FeignException exception = createFeignException(400, invalidJson);

        Span span = mock(Span.class);
        when(tracer.currentSpan()).thenReturn(span);

        // when
        String result = parser.extractMessage(exception);

        // then
        assertThat(result).isEqualTo("Service error: 400");
        verify(span).event("error.parsing.failed");
    }

    @Test
    void shouldWorkWhenTracerHasNoCurrentSpan() throws Exception {
        // given
        BackendErrorResponse backendError = new BackendErrorResponse(
                "2026-02-06T12:00:00",
                401,
                "Unauthorized",
                "Unauthorized access"
        );

        String json = objectMapper.writeValueAsString(backendError);
        FeignException exception = createFeignException(401, json);

        when(tracer.currentSpan()).thenReturn(null);

        // when
        String result = parser.extractMessage(exception);

        // then
        assertThat(result).isEqualTo("Unauthorized access");
    }
}
