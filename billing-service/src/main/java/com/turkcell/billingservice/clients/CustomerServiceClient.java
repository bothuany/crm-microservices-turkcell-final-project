package com.turkcell.billingservice.clients;

import com.turkcell.billingservice.config.FeignClientConfig;
import com.turkcell.billingservice.domain.dtos.responses.CustomerResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * CustomerServiceClient - Customer service client for retrieving customer details.
 */
@FeignClient(
    name = "customer-service",
    path = "/api/v1/customers",
    url = "${services.customer-service.url:#{null}}",
    primary = false,
    configuration = FeignClientConfig.class
)
public interface CustomerServiceClient {
    /**
     * Get customer details by id
     * @param customerId customer id
     * @return customer response
     */
    @GetMapping("/{customerId}")
    @CircuitBreaker(name = "customerService", fallbackMethod = "getCustomerByIdFallback")
    CustomerResponse getCustomerById(@PathVariable UUID customerId);
    
    /**
     * Get customer by ID - alias for getCustomerById for better readability
     * @param customerId customer id
     * @return customer response
     */
    default CustomerResponse getCustomer(UUID customerId) {
        return getCustomerById(customerId);
    }
    
    /**
     * Validates if a customer exists
     * @param customerId customer id
     * @return true if customer exists, false otherwise
     */
    default boolean validateCustomer(UUID customerId) {
        try {
            CustomerResponse response = getCustomerById(customerId);
            return response != null && response.getId() != null;
        } catch (Exception e) {
            System.err.println("Error validating customer " + customerId + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Fallback method for getCustomerById
     * @param customerId customer id
     * @param exception exception that occurred
     * @return default customer response
     */
    default CustomerResponse getCustomerByIdFallback(UUID customerId, Exception exception) {
        System.err.println("Fallback for getCustomerById with id: " + customerId + ", error: " + exception.getMessage());
        CustomerResponse fallback = new CustomerResponse();
        fallback.setId(customerId);
        fallback.setName("Customer Not Available");
        fallback.setEmail("not.available@example.com");
        fallback.setPhone("N/A");
        return fallback;
    }
} 