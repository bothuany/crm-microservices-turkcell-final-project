package com.turkcell.billingservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * İş kuralı ihlallerinde fırlatılan temel exception sınıfı.
 * Bu sınıf, iş mantığı ile ilgili hataları temsil eder.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {
    /**
     * Yeni bir BusinessException oluşturur.
     *
     * @param message Hata mesajı
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Yeni bir BusinessException oluşturur.
     *
     * @param message Hata mesajı
     * @param cause Hatanın nedeni
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
} 