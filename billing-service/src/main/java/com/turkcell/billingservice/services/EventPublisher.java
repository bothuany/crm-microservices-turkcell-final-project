package com.turkcell.billingservice.services;

import com.turkcell.billingservice.domain.entities.Bill;
import com.turkcell.billingservice.domain.entities.Payment;
import com.turkcell.billingservice.domain.entities.Refund;
import com.turkcell.billingservice.domain.enums.BillStatus;
import com.turkcell.billingservice.events.BillCreatedEvent;
import com.turkcell.billingservice.events.PaymentProcessedEvent;
import com.turkcell.billingservice.events.RefundProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Olayları yayınlayan servis sınıfı.
 * Spring ApplicationEventPublisher kullanarak sistem içi olayları yayınlar.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {
    private final ApplicationEventPublisher publisher;

    /**
     * Fatura oluşturuldu olayını yayınlar.
     *
     * @param bill Fatura entity
     */
    public void publishBillCreatedEvent(Bill bill) {
        log.info("Publishing BillCreatedEvent for billId: {}", bill.getId());
        BillCreatedEvent event = BillCreatedEvent.builder()
                .billId(bill.getId())
                .customerId(bill.getCustomerId())
                .totalAmount(bill.getTotalAmount())
                .dueDate(bill.getDueDate())
                .status(bill.getStatus().toString())
                .description(bill.getDescription())
                .build();
        publisher.publishEvent(event);
    }

    /**
     * Ödeme işlendi olayını yayınlar.
     *
     * @param payment Ödeme entity
     */
    public void publishPaymentProcessedEvent(Payment payment) {
        log.info("Publishing PaymentProcessedEvent for paymentId: {}", payment.getId());
        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                .paymentId(payment.getId())
                .billId(payment.getBillId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount().doubleValue())
                .status(payment.getStatus().toString())
                .paymentMethod(payment.getPaymentMethod().toString())
                .processedAt(LocalDateTime.now())
                .build();
        publisher.publishEvent(event);
    }
    
    /**
     * İade işlendi olayını yayınlar.
     *
     * @param refund İade entity
     */
    public void publishRefundProcessedEvent(Refund refund) {
        log.info("Publishing RefundProcessedEvent for refundId: {}", refund.getId());
        RefundProcessedEvent event = RefundProcessedEvent.builder()
                .refundId(refund.getId())
                .paymentId(refund.getPaymentId())
                .billId(refund.getBillId())
                .customerId(refund.getCustomerId())
                .amount(BigDecimal.valueOf(refund.getAmount().doubleValue()))
                .status(refund.getStatus())
                .reason(refund.getReason())
                .processedAt(LocalDateTime.now())
                .build();
        publisher.publishEvent(event);
    }
} 