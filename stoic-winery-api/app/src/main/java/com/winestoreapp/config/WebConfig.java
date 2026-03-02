package com.winestoreapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String IMAGES_PATTERN = "/images/**";
    private static final String API_IMAGES_PATTERN = "/api/images/**";

    @Value("${image.config.path}")
    private String imageConfigPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(IMAGES_PATTERN)
                .addResourceLocations(imageConfigPath);
        registry.addResourceHandler(API_IMAGES_PATTERN)
                .addResourceLocations(imageConfigPath);
    }
}
