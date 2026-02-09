package com.winestoreapp.wineryadminui.core.exception;

import com.winestoreapp.wineryadminui.core.security.UiAuthFilter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
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
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = UiAuthFilter.class))
@Import({GlobalExceptionHandlerTest.TestController.class, GlobalExceptionHandler.class}) // Импортируем сам Handler
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Tracer tracer;

    @BeforeEach
    void setUp() {
        Span mockSpan = mock(Span.class);
        TraceContext mockContext = mock(TraceContext.class);

        when(tracer.currentSpan()).thenReturn(mockSpan);
        when(mockSpan.context()).thenReturn(mockContext);
        when(mockContext.traceId()).thenReturn("test-trace-id");
    }

    @RestController
    static class TestController {
        private final feign.Request dummyRequest = feign.Request.create(
                feign.Request.HttpMethod.GET, "/url", Collections.emptyMap(),
                null, StandardCharsets.UTF_8, null);

        @GetMapping("/feign-notfound")
        public void throwFeignNotFound() {
            throw new feign.FeignException.NotFound("Not Found", dummyRequest, null, null);
        }

        @GetMapping("/feign-other")
        public void throwFeignOther() {
            throw new feign.FeignException.InternalServerError("Error", dummyRequest, null, null);
        }

        @GetMapping("/general")
        public void throwGeneral() {
            throw new RuntimeException("Something went wrong");
        }
    }

    @Test
    void handleGeneralException_shouldRedirectWithFlashAndTraceId() throws Exception {
        mockMvc.perform(get("/general"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/wines"))
                .andExpect(flash().attribute("error", "An unexpected error occurred. Support ID: test-trace-id"));
    }

    @Test
    void handleFeignException_shouldRedirectWithFlash() throws Exception {
        mockMvc.perform(get("/feign-other"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/wines"))
                .andExpect(flash().attribute("error", "Server communication error: 500"));
    }

    @Test
    void handleNotFound_shouldRedirectWithSpecificFlash() throws Exception {
        mockMvc.perform(get("/feign-notfound"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/wines"))
                .andExpect(flash().attribute("error", "The requested object was not found on the server."));
    }
}
