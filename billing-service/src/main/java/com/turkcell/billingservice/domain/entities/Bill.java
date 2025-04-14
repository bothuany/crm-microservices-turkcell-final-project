package com.turkcell.billingservice.domain.entities;

import com.turkcell.billingservice.domain.enums.BillStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Fatura entity'si.
 * Bir fatura, bir müşteriye ait olan ve bir veya daha fazla fatura kalemi içeren bir belgedir.
 */
@Entity
@Table(name = "bills", indexes = {
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_due_date", columnList = "due_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Bill extends BaseEntity {
    /**
     * Faturaya ait müşteri ID'si.
     */
    @Column(name = "customer_id", nullable = false, columnDefinition = "uuid")
    private UUID customerId;

    /**
     * Faturaya ait sözleşme ID'si.
     */
    @Column(name = "contract_id", columnDefinition = "uuid")
    private UUID contractId;

    /**
     * Fatura tutarı.
     */
    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    /**
     * Fatura son ödeme tarihi.
     */
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /**
     * Fatura durumu.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillStatus status;

    /**
     * Fatura ödenme tarihi.
     */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /**
     * Fatura açıklaması.
     */
    private String description;

    /**
     * Fatura kalemleri.
     */
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BillItem> items = new ArrayList<>();
} 