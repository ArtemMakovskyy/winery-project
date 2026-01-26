package com.winestoreapp.wineryadminui.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import feign.Client;
import feign.okhttp.OkHttpClient;

@Configuration
public class FeignConfig {
    @Bean
    public Client feignClient() {
        return new OkHttpClient();
    }
}