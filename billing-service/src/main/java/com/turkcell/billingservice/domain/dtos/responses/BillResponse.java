package com.turkcell.billingservice.domain.dtos.responses;

import com.turkcell.billingservice.domain.enums.BillStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Fatura yanıt DTO'su.
 * Fatura bilgilerini istemciye döndürmek için kullanılır.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillResponse {
    private UUID id;
    private UUID customerId;
    private UUID contractId;
    private Double totalAmount;
    private LocalDate dueDate;
    private BillStatus status;
    private LocalDateTime paidAt;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BillItemResponse> items;
} 