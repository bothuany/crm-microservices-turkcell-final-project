package com.turkcell.billingservice.clients;

import com.turkcell.billingservice.dtos.responses.ContractResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "contract-service", url = "http://localhost:8082")
@CircuitBreaker(name = "contractService")
public interface ContractClient {
    @GetMapping("/api/v1/contracts/{id}")
    ContractResponse getContractById(@PathVariable Long id);
} 