package com.turkcell.billingservice.clients;

import com.turkcell.billingservice.config.FeignClientConfig;
import com.turkcell.billingservice.domain.dtos.responses.ContractResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * ContractServiceClient - Contract service client for retrieving contract details.
 */
@FeignClient(
    name = "contract-service",
    path = "/api/v1/contracts",
    url = "${services.contract-service.url:#{null}}",
    primary = false,
    configuration = FeignClientConfig.class
)
public interface ContractServiceClient {
    /**
     * Get contract details by id
     * @param contractId contract id
     * @return contract response
     */
    @GetMapping("/{contractId}")
    @CircuitBreaker(name = "contractService", fallbackMethod = "getContractByIdFallback")
    ContractResponse getContractById(@PathVariable UUID contractId);
    
    /**
     * Get contract by ID - alias for getContractById for better readability
     * @param contractId contract id
     * @return contract response
     */
    default ContractResponse getContract(UUID contractId) {
        return getContractById(contractId);
    }
    
    /**
     * Fallback method for getContractById
     * @param contractId contract id
     * @param exception exception that occurred
     * @return default contract response
     */
    default ContractResponse getContractByIdFallback(UUID contractId, Exception exception) {
        System.err.println("Fallback for getContractById with id: " + contractId + ", error: " + exception.getMessage());
        ContractResponse fallback = new ContractResponse();
        fallback.setId(contractId);
        fallback.setPlanId("Default Plan");
        fallback.setStartDate(null);
        fallback.setEndDate(null);
        return fallback;
    }
} 