package com.turkcell.billingservice.exceptions;

import java.math.BigDecimal;

/**
 * Geçersiz iade tutarı durumunda fırlatılan exception.
 * Bu exception, iade tutarı ödeme tutarından fazla olduğunda veya negatif olduğunda kullanılır.
 */
public class InvalidRefundAmountException extends BusinessException {
    /**
     * Yeni bir InvalidRefundAmountException oluşturur.
     *
     * @param refundAmount İade edilmek istenen tutar
     * @param paymentAmount Orijinal ödeme tutarı
     */
    public InvalidRefundAmountException(BigDecimal refundAmount, BigDecimal paymentAmount) {
        super(String.format("Refund amount %s cannot be greater than payment amount %s", refundAmount, paymentAmount));
    }

    /**
     * Negatif iade tutarı için yeni bir InvalidRefundAmountException oluşturur.
     *
     * @param refundAmount Negatif iade tutarı
     */
    public InvalidRefundAmountException(BigDecimal refundAmount) {
        super(String.format("Refund amount %s cannot be negative", refundAmount));
    }
} 