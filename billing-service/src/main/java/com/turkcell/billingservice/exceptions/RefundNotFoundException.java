package com.turkcell.billingservice.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a refund with the specified ID cannot be found.
 * This exception extends ResourceNotFoundException to provide consistent error handling
 * for all resource not found scenarios in the application.
 */
public class RefundNotFoundException extends ResourceNotFoundException {

    /**
     * Constructs a new RefundNotFoundException with the specified refund ID.
     *
     * @param refundId the ID of the refund that was not found
     */
    public RefundNotFoundException(UUID refundId) {
        super("Refund with ID: " + refundId + " not found");
    }

    /**
     * Yeni bir RefundNotFoundException oluşturur.
     *
     * @param message Hata mesajı
     */
    public RefundNotFoundException(String message) {
        super(message);
    }
} 