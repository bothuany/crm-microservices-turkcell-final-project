package com.turkcell.billingservice.services;

import com.turkcell.billingservice.dtos.requests.CreateRefundRequest;
import com.turkcell.billingservice.dtos.responses.RefundResponse;
import com.turkcell.billingservice.entities.Refund;
import com.turkcell.billingservice.repositories.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefundService {
    private final RefundRepository refundRepository;
    private final BillService billService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RefundResponse createRefund(CreateRefundRequest request) {
        // İade işlemi başlatılıyor
        Refund refund = Refund.builder()
                .paymentId(request.getPaymentId())
                .billId(request.getBillId())
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .reason(request.getReason())
                .build();

        Refund savedRefund = refundRepository.save(refund);

        // İade işlemini başlat
        processRefund(savedRefund);

        return mapToResponse(savedRefund);
    }

    private void processRefund(Refund refund) {
        // Gerçek bir iade sistemi entegrasyonu burada olacak
        // Şimdilik direkt başarılı kabul ediyoruz
        refund.setStatus("COMPLETED");
        refund.setCompletedAt(LocalDateTime.now());
        Refund updatedRefund = refundRepository.save(refund);

        // Fatura durumunu güncelle
        billService.updateBillStatus(refund.getBillId(), "REFUNDED");

        // Kafka event gönderimi
        kafkaTemplate.send("refund-completed", updatedRefund);
    }

    public RefundResponse getRefund(Long id) {
        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Refund not found"));
        return mapToResponse(refund);
    }

    public List<RefundResponse> getRefundsByCustomer(Long customerId) {
        return refundRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<RefundResponse> getRefundsByBill(Long billId) {
        return refundRepository.findByBillId(billId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<RefundResponse> getRefundsByPayment(Long paymentId) {
        return refundRepository.findByPaymentId(paymentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RefundResponse mapToResponse(Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .paymentId(refund.getPaymentId())
                .billId(refund.getBillId())
                .customerId(refund.getCustomerId())
                .amount(refund.getAmount())
                .status(refund.getStatus())
                .createdAt(refund.getCreatedAt())
                .completedAt(refund.getCompletedAt())
                .reason(refund.getReason())
                .build();
    }
} 