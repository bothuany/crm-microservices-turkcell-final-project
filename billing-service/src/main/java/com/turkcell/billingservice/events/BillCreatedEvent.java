package com.turkcell.billingservice.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Fatura oluşturulduğunda yayınlanan olay sınıfı.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillCreatedEvent {
    private UUID billId;
    private UUID customerId;
    private Double totalAmount;
    private LocalDate dueDate;
    private String status;
    private String description;
} 