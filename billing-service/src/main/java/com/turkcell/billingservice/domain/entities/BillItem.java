package com.turkcell.billingservice.domain.entities;

import com.turkcell.billingservice.domain.enums.ItemType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Fatura kalemi entity'si.
 * Bir fatura kalemi, faturanın detaylı içeriğini oluşturan her bir öğeyi temsil eder.
 */
@Entity
@Table(name = "bill_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BillItem extends BaseEntity {
    /**
     * Fatura kalemi ID'si.
     */
    @Column(name = "bill_id", nullable = false, columnDefinition = "uuid")
    private UUID billId;
    
    /**
     * Fatura kalemi tipi.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    /**
     * Fatura kalemi açıklaması.
     */
    @Column(name = "description", nullable = false)
    private String description;

    /**
     * Fatura kalemi tutarı.
     */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * Fatura kalemi miktarı.
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    /**
     * Fatura kaleminin ait olduğu fatura.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", insertable = false, updatable = false)
    private Bill bill;
} 