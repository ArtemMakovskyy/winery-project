package com.winestoreapp.controller;

import com.winestoreapp.common.exception.CustomGlobalExceptionHandler;
import com.winestoreapp.common.ratelimit.RateLimitInterceptor;
import com.winestoreapp.common.ratelimit.RateLimitService;
import com.winestoreapp.config.ApplicationControllerTestConfig;
import com.winestoreapp.service.RedisDebugService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HealthCheckController.class)
@ComponentScan(basePackages = "com.winestoreapp.controller")
@ContextConfiguration(classes = {
        ApplicationControllerTestConfig.class,
        CustomGlobalExceptionHandler.class,
        RateLimitInterceptor.class
})
class HealthCheckControllerTest {

    @MockBean
    private RateLimitService rateLimitService;

    @MockBean
    private RedisDebugService redisDebugService;

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    @DisplayName("Start healthCheck end point. Return String")
    void healthCheck_ValidData_Success() throws Exception {
        mockMvc.perform(
                        get("/health")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Health check passed. Application is running smoothly."));
    }
}
