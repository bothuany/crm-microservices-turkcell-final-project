package com.turkcell.billingservice.exceptions;

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