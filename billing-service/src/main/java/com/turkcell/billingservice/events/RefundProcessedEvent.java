package com.turkcell.billingservice.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * İade işlendiğinde yayınlanan olay sınıfı.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundProcessedEvent {
    private UUID refundId;
    private UUID paymentId;
    private UUID billId;
    private UUID customerId;
    private BigDecimal amount;
    private String status;
    private String reason;
    private LocalDateTime processedAt;
} 