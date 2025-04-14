package com.turkcell.billingservice.exceptions;

import java.util.UUID;

/**
 * Fatura bulunamadığında fırlatılan exception.
 * Bu exception, istenen faturanın sistemde bulunamadığı durumlarda kullanılır.
 */
public class BillNotFoundException extends ResourceNotFoundException {
    /**
     * Yeni bir BillNotFoundException oluşturur.
     *
     * @param billId Bulunamayan faturanın ID'si
     */
    public BillNotFoundException(UUID billId) {
        super("Bill", billId.toString());
    }
} 