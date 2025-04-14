package com.turkcell.billingservice.domain.entities;

import com.turkcell.billingservice.domain.enums.PaymentMethod;
import com.turkcell.billingservice.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Ödeme entity'si.
 * Bir ödeme, bir fatura için yapılan ödeme işlemini temsil eder.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Payment extends BaseEntity {
    /**
     * Ödemenin yapıldığı fatura ID'si.
     */
    @Column(name = "bill_id", nullable = false, columnDefinition = "uuid")
    private UUID billId;
    
    /**
     * Ödemeyi yapan müşteri ID'si.
     */
    @Column(name = "customer_id", nullable = false, columnDefinition = "uuid")
    private UUID customerId;

    /**
     * Ödeme tutarı.
     */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * Ödeme yöntemi.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    /**
     * Ödeme durumu.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    /**
     * Ödeme referans numarası.
     */
    @Column(name = "reference_number", unique = true)
    private String referenceNumber;
    
    /**
     * Ödeme işlem numarası.
     */
    @Column(name = "transaction_id")
    private String transactionId;
    
    /**
     * Ödemenin tamamlandığı tarih.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Ödemeye ait iadeler.
     */
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Refund> refunds;
} 