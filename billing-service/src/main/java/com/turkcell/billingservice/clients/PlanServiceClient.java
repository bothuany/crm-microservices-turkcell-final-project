package com.turkcell.billingservice.clients;

import com.turkcell.billingservice.config.FeignClientConfig;
import com.turkcell.billingservice.domain.dtos.responses.PlanResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * PlanServiceClient - Plan service client for retrieving plan details.
 */
@FeignClient(
    name = "plan-service",
    path = "/api/v1/plans",
    url = "${services.plan-service.url:#{null}}",
    primary = false,
    configuration = FeignClientConfig.class
)
public interface PlanServiceClient {
    /**
     * Get plan details by id
     * @param planId plan id
     * @return plan response
     */
    @GetMapping("/{planId}")
    @CircuitBreaker(name = "planService", fallbackMethod = "getPlanByIdFallback")
    PlanResponse getPlanById(@PathVariable Long planId);
    
    /**
     * Fallback method for getPlanById
     * @param planId plan id
     * @param exception exception that occurred
     * @return default plan response
     */
    default PlanResponse getPlanByIdFallback(Long planId, Exception exception) {
        System.err.println("Fallback for getPlanById with id: " + planId + ", error: " + exception.getMessage());
        PlanResponse fallback = new PlanResponse();
        fallback.setId(planId);
        fallback.setName("Default Plan");
        fallback.setPrice(0.0);
        return fallback;
    }

    /**
     * Get plan by ID - alias for getPlanById for better readability
     * @param planId plan id
     * @return plan response
     */
    default PlanResponse getPlan(Long planId) {
        return getPlanById(planId);
    }
}