package com.turkcell.billingservice.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * İade entity'si.
 * Bir iade, bir ödeme için yapılan iade işlemini temsil eder.
 */
@Entity
@Table(name = "refunds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Refund extends BaseEntity {
    /**
     * İadenin yapıldığı ödeme ID'si.
     */
    @Column(name = "payment_id", nullable = false, columnDefinition = "uuid")
    private UUID paymentId;
    
    /**
     * İadenin yapıldığı fatura ID'si.
     */
    @Column(name = "bill_id", nullable = false, columnDefinition = "uuid")
    private UUID billId;
    
    /**
     * İadeyi alan müşteri ID'si.
     */
    @Column(name = "customer_id", nullable = false, columnDefinition = "uuid")
    private UUID customerId;

    /**
     * İade tutarı.
     */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * İade nedeni.
     */
    @Column(name = "reason", nullable = false)
    private String reason;

    /**
     * İade referans numarası.
     */
    @Column(name = "reference_number", unique = true)
    private String referenceNumber;
    
    /**
     * İade işlem numarası.
     */
    @Column(name = "transaction_id")
    private String transactionId;
    
    /**
     * İade durumu.
     */
    @Column(name = "status", nullable = false)
    private String status;
    
    /**
     * İadenin tamamlandığı tarih.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    /**
     * İadenin yapıldığı ödeme.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, insertable = false, updatable = false)
    private Payment payment;
} 