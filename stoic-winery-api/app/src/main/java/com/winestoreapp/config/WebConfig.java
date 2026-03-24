package com.winestoreapp.config;

import com.winestoreapp.common.ratelimit.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String IMAGES_PATTERN = "/images/**";
    private static final String API_IMAGES_PATTERN = "/api/images/wine/**";

    @Value("${image.config.path}")
    private String imageConfigPath;

    private final RateLimitInterceptor rateLimitInterceptor;

    public WebConfig(RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Image handlers - must be more specific to avoid conflicts
        registry.addResourceHandler(IMAGES_PATTERN)
                .addResourceLocations(imageConfigPath);
        registry.addResourceHandler(API_IMAGES_PATTERN)
                .addResourceLocations(imageConfigPath);
        
        // IMPORTANT: Do NOT add /api/redis/** handler - let it go to controllers
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/actuator/**",
                        "/api/redis/**"
                );
    }
}
