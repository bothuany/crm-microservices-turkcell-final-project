package com.turkcell.billingservice.clients;

import com.turkcell.billingservice.dtos.CustomerDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "customer-service", url = "http://localhost:8081")
@CircuitBreaker(name = "customerService")
@RateLimiter(name = "customerService")
public interface CustomerClient {
    @GetMapping("/api/v1/customers/{id}")
    CustomerDto getCustomerById(@PathVariable Long id, @RequestHeader("Authorization") String token);
} 