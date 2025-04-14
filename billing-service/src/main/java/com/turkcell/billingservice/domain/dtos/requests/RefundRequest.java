package com.turkcell.billingservice.domain.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    @NotNull(message = "Payment ID cannot be null")
    private UUID paymentId;
    
    @NotNull(message = "Bill ID cannot be null")
    private UUID billId;
    
    @NotNull(message = "Customer ID cannot be null")
    private UUID customerId;
    
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private Double amount;
    
    @NotBlank(message = "Reason cannot be empty")
    private String reason;
} 