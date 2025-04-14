package com.turkcell.billingservice.domain.mappers;

import com.turkcell.billingservice.domain.entities.Payment;
import com.turkcell.billingservice.domain.enums.PaymentMethod;
import com.turkcell.billingservice.domain.dtos.requests.PaymentRequest;
import com.turkcell.billingservice.domain.dtos.responses.PaymentResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Payment entity ve DTO'ları arasında dönüşüm yapar.
 */
@Component
public class PaymentMapper {

    /**
     * PaymentRequest'i Payment entity'ye dönüştürür.
     *
     * @param request PaymentRequest
     * @return Payment entity
     */
    public static Payment toEntity(PaymentRequest request) {
        return Payment.builder()
                .billId(request.getBillId())
                .customerId(request.getCustomerId())
                .amount(java.math.BigDecimal.valueOf(request.getAmount()))
                .paymentMethod(request.getPaymentMethod())
                .status(com.turkcell.billingservice.domain.enums.PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Payment entity'yi PaymentResponse'a dönüştürür.
     *
     * @param payment Payment entity
     * @return PaymentResponse
     */
    public static PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .billId(payment.getBillId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount().doubleValue())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus().toString())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .build();
    }
} 