package com.turkcell.billingservice.domain.enums;

import com.turkcell.billingservice.domain.exceptions.BillingException;

/**
 * Fatura durumlarını temsil eden enum.
 * Bir faturanın yaşam döngüsündeki farklı durumları tanımlar.
 */
public enum BillStatus {
    /**
     * Fatura oluşturuldu ancak henüz ödenmedi.
     */
    PENDING,

    /**
     * Fatura tamamen ödendi.
     */
    PAID,

    /**
     * Fatura son ödeme tarihi geçti ve hala ödenmedi.
     */
    OVERDUE,

    /**
     * Fatura için iade işlemi yapıldı.
     */
    REFUNDED,

    /**
     * Fatura iptal edildi.
     */
    CANCELLED;

    public static BillStatus fromString(String status) {
        try {
            return BillStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BillingException("Invalid bill status: " + status);
        }
    }
} 