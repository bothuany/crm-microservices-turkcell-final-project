package com.turkcell.billingservice.domain.dtos.requests;

import com.turkcell.billingservice.domain.enums.ItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

/**
 * Fatura kalemlerinin oluşturulması için kullanılan istek DTO'su.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillItemRequest {
    @NotNull(message = "Item type cannot be null")
    private ItemType itemType;
    
    @NotBlank(message = "Description cannot be empty")
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private Double amount;
    
    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
} 