package com.turkcell.billingservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long billId;
    private Long customerId;
    private Double amount;
    private String status;
    private String paymentMethod;
    private LocalDateTime paymentDate;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 