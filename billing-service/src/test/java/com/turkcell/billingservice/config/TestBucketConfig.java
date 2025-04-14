package com.turkcell.billingservice.config;

import io.github.bucket4j.Bucket;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestBucketConfig {
    
    @Bean
    @Primary
    public Bucket defaultBucket() {
        return mock(Bucket.class);
    }
    
    @Bean("billApiBucket")
    @Primary
    public Bucket billApiBucket() {
        return mock(Bucket.class);
    }
    
    @Bean("paymentApiBucket")
    @Primary
    public Bucket paymentApiBucket() {
        return mock(Bucket.class);
    }
    
    @Bean("reportApiBucket")
    @Primary
    public Bucket reportApiBucket() {
        return mock(Bucket.class);
    }
} 