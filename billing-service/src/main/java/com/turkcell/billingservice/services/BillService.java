package com.turkcell.billingservice.services;

import com.turkcell.billingservice.dtos.requests.CreateBillRequest;
import com.turkcell.billingservice.dtos.responses.BillResponse;
import com.turkcell.billingservice.entities.Bill;
import com.turkcell.billingservice.repositories.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillService {
    private final BillRepository billRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BillResponse createBill(CreateBillRequest request) {
        Bill bill = Bill.builder()
                .customerId(request.getCustomerId())
                .contractId(request.getContractId())
                .amount(request.getAmount())
                .status("PENDING")
                .dueDate(request.getDueDate())
                .createdAt(LocalDateTime.now())
                .description(request.getDescription())
                .build();

        Bill savedBill = billRepository.save(bill);
        
        // Kafka event gönderimi
        kafkaTemplate.send("bill-created", savedBill);

        return mapToResponse(savedBill);
    }

    public BillResponse getBill(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        return mapToResponse(bill);
    }

    public List<BillResponse> getBillsByCustomer(Long customerId) {
        return billRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<BillResponse> getBillsByStatus(String status) {
        return billRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<BillResponse> getBillsByCustomerAndDateRange(Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        return billRepository.findByCustomerIdAndCreatedAtBetween(customerId, startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void updateBillStatus(Long id, String status) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        
        bill.setStatus(status);
        if (status.equals("PAID")) {
            bill.setPaidAt(LocalDateTime.now());
        }
        
        Bill updatedBill = billRepository.save(bill);
        
        // Kafka event gönderimi
        kafkaTemplate.send("bill-status-updated", updatedBill);
    }

    private BillResponse mapToResponse(Bill bill) {
        return BillResponse.builder()
                .id(bill.getId())
                .customerId(bill.getCustomerId())
                .contractId(bill.getContractId())
                .amount(bill.getAmount())
                .status(bill.getStatus())
                .dueDate(bill.getDueDate())
                .createdAt(bill.getCreatedAt())
                .paidAt(bill.getPaidAt())
                .description(bill.getDescription())
                .build();
    }
} 