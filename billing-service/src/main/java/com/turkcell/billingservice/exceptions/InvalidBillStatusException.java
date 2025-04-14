package com.turkcell.billingservice.exceptions;

import com.turkcell.billingservice.domain.enums.BillStatus;

/**
 * Geçersiz fatura durumu değişikliklerinde fırlatılan exception.
 * Bu exception, bir faturanın durumu geçersiz bir şekilde değiştirilmeye çalışıldığında kullanılır.
 */
public class InvalidBillStatusException extends BusinessException {
    /**
     * Yeni bir InvalidBillStatusException oluşturur.
     *
     * @param currentStatus Faturanın mevcut durumu
     * @param targetStatus Hedeflenen durum
     */
    public InvalidBillStatusException(BillStatus currentStatus, BillStatus targetStatus) {
        super(String.format("Cannot change bill status from %s to %s", currentStatus, targetStatus));
    }
} 