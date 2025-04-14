package com.turkcell.billingservice.domain.enums;

/**
 * Ödeme yöntemlerini temsil eden enum.
 * Bir ödemenin hangi yöntemle yapıldığını tanımlar.
 */
public enum PaymentMethod {
    /**
     * Kredi kartı ile ödeme
     */
    CREDIT_CARD,

    /**
     * Banka havalesi ile ödeme
     */
    BANK_TRANSFER,

    /**
     * Nakit ödeme
     */
    CASH,

    /**
     * Mobil ödeme
     */
    MOBILE_PAYMENT,
    
    /**
     * Bilinmeyen ödeme yöntemi
     */
    UNKNOWN
} 