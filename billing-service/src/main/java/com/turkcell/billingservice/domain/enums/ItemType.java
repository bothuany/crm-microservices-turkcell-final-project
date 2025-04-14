package com.turkcell.billingservice.domain.enums;

/**
 * Fatura kalemlerinin tiplerini tanımlayan enum.
 */
public enum ItemType {
    /**
     * Sabit ücret kalemlerini temsil eder (Örn: Aylık temel abonelik ücreti).
     */
    FIXED_FEE,
    
    /**
     * Kullanım bazlı ücretleri temsil eder (Örn: Dakika başına konuşma ücreti).
     */
    USAGE_FEE,
    
    /**
     * Ek paket ücretlerini temsil eder (Örn: Ek internet paketi ücreti).
     */
    EXTRA_PACKAGE_FEE,
    
    /**
     * Tek seferlik ücretleri temsil eder (Örn: Aktivasyon ücreti).
     */
    ONE_TIME_FEE,
    
    /**
     * Vergiler için kullanılır (Örn: KDV).
     */
    TAX,
    
    /**
     * İndirimler için kullanılır.
     */
    DISCOUNT,
    
    /**
     * Diğer ücretler için kullanılır.
     */
    OTHER
} 