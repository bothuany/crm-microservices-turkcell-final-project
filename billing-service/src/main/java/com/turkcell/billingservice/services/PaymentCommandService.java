package com.turkcell.billingservice.services;

import com.turkcell.billingservice.clients.CustomerServiceClient;
import com.turkcell.billingservice.domain.dtos.requests.NotificationRequest;
import com.turkcell.billingservice.domain.dtos.requests.PaymentRequest;
import com.turkcell.billingservice.domain.dtos.requests.RefundRequest;
import com.turkcell.billingservice.domain.dtos.responses.CustomerResponse;
import com.turkcell.billingservice.domain.dtos.responses.PaymentResponse;
import com.turkcell.billingservice.domain.dtos.responses.RefundResponse;
import com.turkcell.billingservice.domain.entities.Bill;
import com.turkcell.billingservice.domain.entities.Payment;
import com.turkcell.billingservice.domain.entities.Refund;
import com.turkcell.billingservice.domain.enums.BillStatus;
import com.turkcell.billingservice.domain.enums.PaymentStatus;
import com.turkcell.billingservice.exceptions.BillingException;
import com.turkcell.billingservice.exceptions.RefundNotFoundException;
import com.turkcell.billingservice.repositories.BillRepository;
import com.turkcell.billingservice.repositories.PaymentRepository;
import com.turkcell.billingservice.repositories.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ödeme komut işlemlerini yönetir.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCommandService {
    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final RefundRepository refundRepository;
    private final CustomerServiceClient customerServiceClient;
    private final EventPublisher eventPublisher;
    private final NotificationHandler notificationHandler;
    private final PaymentGatewayService paymentGatewayService;

    /**
     * Ödeme işlemini gerçekleştirir.
     *
     * @param request Ödeme isteği
     * @return Ödeme sonucu
     */
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for billId: {}, customerId: {}", request.getBillId(), request.getCustomerId());
        
        // 1. Fatura kontrolü
        Bill bill = billRepository.findById(request.getBillId())
                .orElseThrow(() -> new BillingException("Bill not found with id: " + request.getBillId()));
        
        // 2. Fatura durumu kontrolü
        if (bill.getStatus() != BillStatus.PENDING && bill.getStatus() != BillStatus.OVERDUE) {
            throw new BillingException("Bill is not in a payable status. Current status: " + bill.getStatus());
        }
        
        // 3. Müşteri kontrolü
        if (!bill.getCustomerId().equals(request.getCustomerId())) {
            throw new BillingException("Bill does not belong to this customer");
        }
        
        // 4. Tutar kontrolü
        if (request.getAmount() < bill.getTotalAmount()) {
            throw new BillingException("Payment amount is less than the bill amount");
        }
        
        try {
            // 5. Ödeme işlemi
            String transactionId = paymentGatewayService.processPayment(request.getAmount(), request.getPaymentMethod().toString());
            
            // 6. Ödeme kaydı
            Payment payment = Payment.builder()
                    .billId(request.getBillId())
                    .customerId(request.getCustomerId())
                    .amount(BigDecimal.valueOf(request.getAmount()))
                    .paymentMethod(request.getPaymentMethod())
                    .status(PaymentStatus.SUCCESS)
                    .transactionId(transactionId)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            Payment savedPayment = paymentRepository.save(payment);
            
            // 7. Fatura durumunu güncelle
            bill.setStatus(BillStatus.PAID);
            bill.setUpdatedAt(LocalDateTime.now());
            billRepository.save(bill);
            
            // 8. Olayları yayınla
            eventPublisher.publishPaymentProcessedEvent(savedPayment);
            
            // 9. Bildirim gönder
            CustomerResponse customer = customerServiceClient.getCustomer(request.getCustomerId());
            notificationHandler.sendPaymentConfirmation(savedPayment, customer);
            
            log.info("Payment processed successfully for billId: {}, paymentId: {}", request.getBillId(), savedPayment.getId());
            
            return PaymentResponse.builder()
                    .id(savedPayment.getId())
                    .billId(savedPayment.getBillId())
                    .customerId(savedPayment.getCustomerId())
                    .amount(savedPayment.getAmount().doubleValue())
                    .paymentMethod(request.getPaymentMethod())
                    .status(savedPayment.getStatus().toString())
                    .transactionId(savedPayment.getTransactionId())
                    .createdAt(savedPayment.getCreatedAt())
                    .build();
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage());
            throw new BillingException("Payment processing failed: " + e.getMessage());
        }
    }

    /**
     * Ödeme kaydını siler.
     *
     * @param paymentId Ödeme ID'si
     */
    @Transactional
    public void deletePayment(UUID paymentId) {
        log.info("Deleting payment with ID: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BillingException("Payment not found with id: " + paymentId));
        
        // Önemli not: Gerçek sistemlerde ödeme kaydı genellikle silinmez, iptal veya iade işlemi uygulanır.
        // Bu sadece test amaçlı bir silme işlemidir.
        paymentRepository.delete(payment);
        log.info("Payment deleted with ID: {}", paymentId);
    }

    @Transactional
    public RefundResponse processRefund(RefundRequest request) {
        log.info("Processing refund for paymentId: {}, billId: {}", request.getPaymentId(), request.getBillId());
        
        // 1. Ödeme kontrolü
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new BillingException("Payment not found with id: " + request.getPaymentId()));
        
        // 2. Fatura kontrolü
        Bill bill = billRepository.findById(request.getBillId())
                .orElseThrow(() -> new BillingException("Bill not found with id: " + request.getBillId()));
        
        // 3. Ödeme durumu kontrolü
        if (payment.getStatus() != PaymentStatus.SUCCESS && payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BillingException("Payment status must be SUCCESS or COMPLETED for refund. Current status: " + payment.getStatus());
        }
        
        // 4. Fatura durumu kontrolü
        if (bill.getStatus() != BillStatus.PAID) {
            throw new BillingException("Bill status must be PAID for refund. Current status: " + bill.getStatus());
        }
        
        // 5. Müşteri kontrolü
        if (!payment.getCustomerId().equals(request.getCustomerId())) {
            throw new BillingException("Payment does not belong to this customer");
        }
        
        // 6. İade tutarı kontrolü
        BigDecimal refundAmount = BigDecimal.valueOf(request.getAmount());
        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new BillingException("Refund amount cannot be greater than payment amount");
        }
        
        try {
            // 7. İade işlemi
            String refundTransactionId = paymentGatewayService.processRefund(
                request.getAmount(), 
                payment.getPaymentMethod().toString()
            );
            
            // 8. İade kaydı
            Refund refund = Refund.builder()
                    .paymentId(request.getPaymentId())
                    .billId(request.getBillId())
                    .customerId(payment.getCustomerId())
                    .amount(BigDecimal.valueOf(request.getAmount()))
                    .status(PaymentStatus.SUCCESS.toString())
                    .transactionId(refundTransactionId)
                    .reason(request.getReason())
                    .createdAt(LocalDateTime.now())
                    .completedAt(LocalDateTime.now())
                    .build();
            
            Refund savedRefund = refundRepository.save(refund);
            
            // 9. Fatura durumunu güncelle
            bill.setStatus(BillStatus.REFUNDED);
            bill.setUpdatedAt(LocalDateTime.now());
            billRepository.save(bill);
            
            // 10. Olayları yayınla
            eventPublisher.publishRefundProcessedEvent(savedRefund);
            
            // 11. Bildirim gönder
            CustomerResponse customer = customerServiceClient.getCustomer(payment.getCustomerId());
            notificationHandler.sendRefundConfirmation(savedRefund, customer);
            
            log.info("Refund processed successfully for paymentId: {}, refundId: {}", 
                    request.getPaymentId(), savedRefund.getId());
            
            return RefundResponse.builder()
                    .id(savedRefund.getId())
                    .paymentId(savedRefund.getPaymentId())
                    .billId(savedRefund.getBillId())
                    .customerId(savedRefund.getCustomerId())
                    .amount(savedRefund.getAmount().doubleValue())
                    .status(savedRefund.getStatus())
                    .transactionId(savedRefund.getTransactionId())
                    .reason(savedRefund.getReason())
                    .createdAt(savedRefund.getCreatedAt())
                    .completedAt(savedRefund.getCompletedAt())
                    .build();
        } catch (Exception e) {
            log.error("Error processing refund: {}", e.getMessage());
            throw new BillingException("Refund processing failed: " + e.getMessage());
        }
    }

    public List<RefundResponse> getRefundsByPayment(UUID paymentId) {
        return refundRepository.findByPaymentId(paymentId)
                .stream()
                .map(this::mapToRefundResponse)
                .collect(Collectors.toList());
    }

    public List<RefundResponse> getRefundsByBill(UUID billId) {
        return refundRepository.findByBillId(billId)
                .stream()
                .map(this::mapToRefundResponse)
                .collect(Collectors.toList());
    }

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

    /**
     * İade kaydını siler.
     *
     * @param refundId İade ID'si
     */
    @Transactional
    public void deleteRefund(UUID refundId) {
        log.info("Deleting refund with ID: {}", refundId);
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundNotFoundException(refundId));
        
        // Önemli not: Gerçek sistemlerde iade kaydı genellikle silinmez, iptal işaretlenir.
        // Bu sadece test amaçlı bir silme işlemidir.
        refundRepository.delete(refund);
        log.info("Refund deleted with ID: {}", refundId);
    }
} 