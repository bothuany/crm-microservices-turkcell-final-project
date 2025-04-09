package com.turkcell.billingservice.services;

import com.turkcell.billingservice.clients.PaymentClient;
import com.turkcell.billingservice.dtos.requests.CreatePaymentRequest;
import com.turkcell.billingservice.dtos.responses.PaymentResponse;
import com.turkcell.billingservice.entities.Bill;
import com.turkcell.billingservice.entities.Payment;
import com.turkcell.billingservice.events.PaymentCompletedEvent;
import com.turkcell.billingservice.exceptions.BusinessException;
import com.turkcell.billingservice.exceptions.ResourceNotFoundException;
import com.turkcell.billingservice.repositories.BillRepository;
import com.turkcell.billingservice.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final PaymentClient paymentClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        // Validate bill
        Bill bill = billRepository.findById(request.getBillId())
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (!"PENDING".equals(bill.getStatus())) {
            throw new BusinessException("Bill is not in PENDING status");
        }

        // Process payment through payment service
        PaymentResponse paymentResponse = paymentClient.processPayment(request);

        // Create and save payment record
        Payment payment = Payment.builder()
                .billId(bill.getId())
                .customerId(bill.getCustomerId())
                .amount(paymentResponse.getAmount())
                .status(paymentResponse.getStatus())
                .paymentMethod(paymentResponse.getPaymentMethod())
                .paymentDate(paymentResponse.getPaymentDate())
                .transactionId(paymentResponse.getTransactionId())
                .build();

        payment = paymentRepository.save(payment);

        // Update bill status
        bill.setStatus("PAID");
        bill.setPaidAt(payment.getPaymentDate());
        billRepository.save(bill);

        // Send event
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(payment.getId())
                .billId(payment.getBillId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .build();

        kafkaTemplate.send("payment-completed", event);

        return mapToResponse(payment);
    }

    public PaymentResponse getPayment(Long id) {
        return mapToResponse(findPaymentById(id));
    }

    public List<PaymentResponse> getPaymentsByBill(Long billId) {
        return paymentRepository.findByBillId(billId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getPaymentsByCustomer(Long customerId) {
        return paymentRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getPaymentsByCustomerAndDateRange(Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.findByCustomerIdAndCreatedAtTimestampBetween(customerId, startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Payment findPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .billId(payment.getBillId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .paymentDate(payment.getPaymentDate())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAtTimestamp())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
} 