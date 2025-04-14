package com.turkcell.billingservice.services;

import com.turkcell.billingservice.domain.dtos.responses.PaymentResponse;
import com.turkcell.billingservice.domain.entities.Payment;
import com.turkcell.billingservice.domain.enums.PaymentMethod;
import com.turkcell.billingservice.exceptions.BillingException;
import com.turkcell.billingservice.repositories.PaymentRepository;
import com.turkcell.billingservice.repositories.RefundRepository;
import com.turkcell.billingservice.exceptions.RefundNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.turkcell.billingservice.domain.entities.Refund;
import com.turkcell.billingservice.domain.dtos.responses.RefundResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import io.github.bucket4j.Bucket;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Ödeme sorgulama işlemlerini yönetir.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentQueryService {
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    @Qualifier("billApiBucket")
    private final Bucket rateLimitBucket;

    /**
     * Ödeme detaylarını getirir.
     *
     * @param paymentId Ödeme ID'si
     * @return Ödeme detayları
     */
    public PaymentResponse getPayment(UUID paymentId) {
        log.info("Fetching payment with ID: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BillingException("Payment not found with id: " + paymentId));
        return mapToResponse(payment);
    }

    /**
     * Müşteriye göre ödemeleri getirir.
     *
     * @param customerId Müşteri ID'si
     * @param pageable Sayfalama bilgisi
     * @return Ödemeler listesi
     */
    public Page<PaymentResponse> getPaymentsByCustomer(UUID customerId, Pageable pageable) {
        log.info("Fetching payments for customer ID: {}", customerId);
        Page<Payment> payments = paymentRepository.findByCustomerId(customerId, pageable);
        return payments.map(this::mapToResponse);
    }

    /**
     * Faturaya göre ödemeleri getirir.
     *
     * @param billId Fatura ID'si
     * @param pageable Sayfalama bilgisi
     * @return Ödemeler listesi
     */
    public Page<PaymentResponse> getPaymentsByBill(UUID billId, Pageable pageable) {
        log.info("Fetching payments for bill ID: {}", billId);
        Page<Payment> payments = paymentRepository.findByBillId(billId, pageable);
        return payments.map(this::mapToResponse);
    }

    /**
     * Kullanılabilir ödeme yöntemlerini getirir.
     *
     * @return Ödeme yöntemleri listesi
     */
    @Cacheable("paymentMethods")
    public List<String> getPaymentMethods() {
        log.info("Fetching available payment methods");
        return Arrays.stream(PaymentMethod.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    /**
     * Ödemenin belirtilen müşteriye ait olup olmadığını kontrol eder.
     *
     * @param paymentId Ödeme ID'si
     * @param username Müşteri ID'si
     * @return Ödemenin müşteriye ait olup olmadığı
     */
    public boolean isPaymentBelongsToCustomer(UUID paymentId, String username) {
        UUID customerId = UUID.fromString(username);
        return paymentRepository.existsByIdAndCustomerId(paymentId, customerId);
    }

    /**
     * İadenin belirtilen müşteriye ait olup olmadığını kontrol eder.
     *
     * @param refundId İade ID'si
     * @param username Müşteri ID'si
     * @return İadenin müşteriye ait olup olmadığı
     */
    public boolean isRefundBelongsToCustomer(UUID refundId, String username) {
        UUID customerId = UUID.fromString(username);
        return refundRepository.existsByIdAndCustomerId(refundId, customerId);
    }

    /**
     * ID'ye göre iade detaylarını getirir.
     *
     * @param refundId İade ID'si
     * @return İade detayları
     * @throws RefundNotFoundException İade bulunamazsa
     */
    public RefundResponse getRefund(UUID refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundNotFoundException(refundId));
        return mapToRefundResponse(refund);
    }

    /**
     * İade entity'sini RefundResponse DTO'ya dönüştürür.
     *
     * @param refund İade entity
     * @return RefundResponse DTO
     */
    private RefundResponse mapToRefundResponse(Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .paymentId(refund.getPaymentId())
                .billId(refund.getBillId())
                .customerId(refund.getCustomerId())
                .amount(refund.getAmount().doubleValue())
                .status(refund.getStatus())
                .transactionId(refund.getTransactionId())
                .reason(refund.getReason())
                .createdAt(refund.getCreatedAt())
                .completedAt(refund.getCompletedAt())
                .build();
    }

    private PaymentResponse mapToResponse(Payment payment) {
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