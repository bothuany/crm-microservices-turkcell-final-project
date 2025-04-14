package com.turkcell.billingservice.domain.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Müşteri bilgilerini temsil eden DTO sınıfı.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
} 