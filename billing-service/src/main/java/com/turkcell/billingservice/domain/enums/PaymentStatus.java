package com.turkcell.billingservice.domain.enums;

/**
 * Ödeme durumlarını temsil eden enum.
 * Bir ödemenin yaşam döngüsündeki farklı durumları tanımlar.
 */
public enum PaymentStatus {
    /**
     * Ödeme oluşturuldu ancak henüz tamamlanmadı.
     */
    PENDING,

    /**
     * Ödeme başarıyla tamamlandı.
     */
    SUCCESS,
    
    /**
     * Ödeme başarıyla tamamlandı.
     */
    COMPLETED,

    /**
     * Ödeme işlemi başarısız oldu.
     */
    FAILED,

    /**
     * Ödeme için iade işlemi yapıldı.
     */
    REFUNDED
} 