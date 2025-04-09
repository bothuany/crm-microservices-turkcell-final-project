package com.turkcell.billingservice.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {
    private Long billId;
    private Double amount;
    private String paymentMethod;
    private String cardNumber;
    private String cardHolderName;
    private String expirationDate;
    private String cvv;
} 