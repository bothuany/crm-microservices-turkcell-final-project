package com.turkcell.billingservice.domain.dtos.responses;

import com.turkcell.billingservice.domain.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private UUID id;
    private UUID billId;
    private UUID customerId;
    private Double amount;
    private PaymentMethod paymentMethod;
    private String status;
    private String transactionId;
    private LocalDateTime createdAt;
} 