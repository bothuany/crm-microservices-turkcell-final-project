package com.turkcell.billingservice.domain.dtos.requests;

import com.turkcell.billingservice.domain.enums.ItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillRequest {
    @NotNull(message = "Customer ID cannot be null")
    private UUID customerId;
    
    @NotNull(message = "Contract ID cannot be null")
    private UUID contractId;
    
    @NotNull(message = "Total amount cannot be null")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than zero")
    private Double totalAmount;
    
    @NotNull(message = "Due date cannot be null")
    @FutureOrPresent(message = "Due date must be present or future date")
    private LocalDate dueDate;
    
    @NotEmpty(message = "Bill must contain at least one item")
    private List<BillItemRequest> items;
    
    private String description;
} 