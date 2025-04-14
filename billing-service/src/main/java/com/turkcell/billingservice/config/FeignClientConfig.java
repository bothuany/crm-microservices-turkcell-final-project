package com.turkcell.billingservice.config;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign Client Configuration
 */
@Configuration
public class FeignClientConfig {
    
    /**
     * Sets the logging level for Feign clients.
     * @return Logger.Level
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
    
    /**
     * Sets the timeout for Feign clients.
     * @return Request.Options
     */
    @Bean
    public Request.Options options() {
        return new Request.Options(5000, 10000);
    }
    
    /**
     * Adds authorization headers to Feign client requests.
     * @return RequestInterceptor
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Add any headers needed for service-to-service communication
            requestTemplate.header("Accept", "application/json");
            requestTemplate.header("Content-Type", "application/json");
        };
    }
} 