package com.turkcell.billingservice.domain.exceptions;

/**
 * Fatura işlemlerinde oluşan hataları temsil eder.
 */
public class BillingException extends RuntimeException {
    public BillingException(String message) {
        super(message);
    }
    
    public BillingException(String message, Throwable cause) {
        super(message, cause);
    }
} 