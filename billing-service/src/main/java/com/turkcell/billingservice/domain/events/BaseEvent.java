package com.turkcell.billingservice.domain.events;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tüm event'ler için ortak alanları içeren temel sınıf.
 * Bu sınıf, tüm event'lerde bulunması gereken ortak alanları tanımlar.
 */
@Getter
@Setter
public abstract class BaseEvent {
    /**
     * Event'in benzersiz kimliği.
     */
    private UUID eventId;

    /**
     * Event'in oluşturulma zamanı.
     */
    private LocalDateTime timestamp;

    /**
     * Event'in tipi.
     */
    private String eventType;

    /**
     * Event'in oluşturulduğu entity'nin ID'si.
     */
    private UUID entityId;

    protected BaseEvent(String eventType, UUID entityId) {
        this.eventId = UUID.randomUUID();
        this.timestamp = LocalDateTime.now();
        this.eventType = eventType;
        this.entityId = entityId;
    }
} 