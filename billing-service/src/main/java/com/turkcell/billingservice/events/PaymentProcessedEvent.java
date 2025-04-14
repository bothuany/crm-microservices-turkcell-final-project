package com.turkcell.billingservice.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Ödeme işlendiğinde yayınlanan olay sınıfı.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedEvent {
    private UUID paymentId;
    private UUID billId;
    private UUID customerId;
    private Double amount;
    private String status;
    private String paymentMethod;
    private LocalDateTime processedAt;
} 