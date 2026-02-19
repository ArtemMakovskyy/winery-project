package com.winestoreapp.wineryadminui.core.exception;

import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import com.winestoreapp.wineryadminui.core.security.UiAuthFilter;
import com.winestoreapp.wineryadminui.core.util.FeignErrorParser;
import feign.FeignException;
import feign.Request;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = GlobalExceptionHandlerTest.TestController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = UiAuthFilter.class)
)
@Import({GlobalExceptionHandlerTest.TestController.class, GlobalExceptionHandler.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Tracer tracer;

    @MockBean
    private SpanTagger spanTagger;

    @MockBean
    private FeignErrorParser feignErrorParser;

    @BeforeEach
    void setUp() {
        Span mockSpan = mock(Span.class);
        TraceContext mockContext = mock(TraceContext.class);

        when(tracer.currentSpan()).thenReturn(mockSpan);
        when(mockSpan.context()).thenReturn(mockContext);
        when(mockContext.traceId()).thenReturn("test-trace-id");

        when(spanTagger.traceId()).thenReturn("test-trace-id");
    }

    @Test
    void handleGeneralException_shouldRedirectWithFlashAndTraceId() throws Exception {
        mockMvc.perform(get("/general"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/wines"))
                .andExpect(flash().attribute("error", "An unexpected error occurred. Trace ID: test-trace-id"));
    }

    @Test
    void handleFeignException_shouldRedirectWithFlash() throws Exception {
        when(feignErrorParser.extractMessage(org.mockito.ArgumentMatchers.any(FeignException.class)))
                .thenReturn("Server communication error: 500");

        mockMvc.perform(get("/feign-other"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/wines"))
                .andExpect(flash().attribute("error", "Server communication error: 500. Support ID: test-trace-id"));
    }

    @Test
    void handleNotFound_shouldRedirectWithSpecificFlash() throws Exception {
        mockMvc.perform(get("/feign-notfound"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/wines"))
                .andExpect(flash().attribute("error", "The requested object was not found."));
    }

    @RestController
    static class TestController {
        private final Request dummyRequest = Request.create(
                Request.HttpMethod.GET, "/url", Collections.emptyMap(),
                null, StandardCharsets.UTF_8, null
        );

        @GetMapping("/feign-notfound")
        public void throwFeignNotFound() {
            throw new FeignException.NotFound("Not Found", dummyRequest, null, null);
        }

        @GetMapping("/feign-other")
        public void throwFeignOther() {
            throw new FeignException.InternalServerError("Error", dummyRequest, null, null);
        }

        @GetMapping("/general")
        public void throwGeneral() {
            throw new RuntimeException("Something went wrong");
        }
    }
}
