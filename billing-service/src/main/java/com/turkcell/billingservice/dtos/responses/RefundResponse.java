package com.turkcell.billingservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {
    private Long id;
    private Long paymentId;
    private Long billId;
    private Long customerId;
    private Double amount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String reason;
} 