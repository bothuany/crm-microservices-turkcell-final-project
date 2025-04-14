package com.turkcell.billingservice.domain.dtos.responses;

import com.turkcell.billingservice.domain.enums.ItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Fatura kalemi yanıt DTO'su.
 * Fatura kalemi bilgilerini istemciye döndürmek için kullanılır.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillItemResponse {
    private UUID id;
    private UUID billId;
    private ItemType itemType;
    private String description;
    private BigDecimal amount;
    private Integer quantity;
    private LocalDateTime createdAt;
} 