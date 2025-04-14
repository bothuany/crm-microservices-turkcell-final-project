package com.turkcell.billingservice.exceptions;

import java.util.UUID;

/**
 * Ödeme bulunamadığında fırlatılan exception.
 * Bu exception, istenen ödemenin sistemde bulunamadığı durumlarda kullanılır.
 */
public class PaymentNotFoundException extends ResourceNotFoundException {
    /**
     * Yeni bir PaymentNotFoundException oluşturur.
     *
     * @param paymentId Bulunamayan ödemenin ID'si
     */
    public PaymentNotFoundException(UUID paymentId) {
        super("Payment", paymentId.toString());
    }
} 