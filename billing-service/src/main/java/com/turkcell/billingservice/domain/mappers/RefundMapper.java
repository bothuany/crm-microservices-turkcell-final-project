package com.turkcell.billingservice.domain.mappers;

import com.turkcell.billingservice.domain.entities.Refund;
import com.turkcell.billingservice.domain.dtos.requests.RefundRequest;
import com.turkcell.billingservice.domain.dtos.responses.RefundResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Refund entity ve DTO'ları arasında dönüşüm yapar.
 */
@Component
public class RefundMapper {

    /**
     * RefundRequest'i Refund entity'ye dönüştürür.
     *
     * @param request RefundRequest
     * @return Refund entity
     */
    public static Refund toEntity(RefundRequest request) {
        return Refund.builder()
                .paymentId(request.getPaymentId())
                .billId(request.getBillId())
                .customerId(request.getCustomerId())
                .amount(BigDecimal.valueOf(request.getAmount()))
                .reason(request.getReason())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Refund entity'yi RefundResponse'a dönüştürür.
     *
     * @param refund Refund entity
     * @return RefundResponse
     */
    public static RefundResponse toResponse(Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .paymentId(refund.getPaymentId())
                .billId(refund.getBillId())
                .customerId(refund.getCustomerId())
                .amount(refund.getAmount().doubleValue())
                .reason(refund.getReason())
                .status(refund.getStatus())
                .transactionId(refund.getTransactionId())
                .createdAt(refund.getCreatedAt())
                .completedAt(refund.getCompletedAt())
                .build();
    }
} 