package com.turkcell.billingservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Kaynak bulunamadığında fırlatılan temel exception sınıfı.
 * Bu sınıf, tüm "not found" exceptionları için base class olarak kullanılır.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Yeni bir ResourceNotFoundException oluşturur.
     *
     * @param resourceName Bulunamayan kaynağın tipi (örn: "Bill", "Payment", "Refund")
     * @param resourceId Bulunamayan kaynağın ID'si
     */
    public ResourceNotFoundException(String resourceName, String resourceId) {
        super(String.format("%s not found with id: %s", resourceName, resourceId));
    }

    /**
     * Yeni bir ResourceNotFoundException oluşturur.
     *
     * @param message Hata mesajı
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
} 