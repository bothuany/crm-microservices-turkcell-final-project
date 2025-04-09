package com.turkcell.billingservice.services;

import com.turkcell.billingservice.clients.CustomerClient;
import com.turkcell.billingservice.clients.ContractClient;
import com.turkcell.billingservice.dtos.CustomerDto;
import com.turkcell.billingservice.dtos.requests.CreateBillRequest;
import com.turkcell.billingservice.dtos.responses.BillResponse;
import com.turkcell.billingservice.dtos.responses.CustomerResponse;
import com.turkcell.billingservice.dtos.responses.ContractResponse;
import com.turkcell.billingservice.entities.Bill;
import com.turkcell.billingservice.events.BillCreatedEvent;
import com.turkcell.billingservice.exceptions.BusinessException;
import com.turkcell.billingservice.exceptions.ResourceNotFoundException;
import com.turkcell.billingservice.repositories.BillRepository;
import com.turkcell.billingservice.utils.BillMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {
    private final BillRepository billRepository;
    // private final CustomerClient customerClient;
    // private final ContractClient contractClient;
    // private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public BillResponse createBill(CreateBillRequest request) {
        log.info("Creating bill for customer: {}", request.getCustomerId());
        
        try {
            // Test için Feign client kontrolünü bypass ediyoruz
            // CustomerDto customer = customerClient.getCustomerById(request.getCustomerId(), "Bearer test-token");
            // if (customer == null) {
            //     throw new RuntimeException("Customer not found");
            // }

            // Fatura oluştur
            Bill bill = Bill.builder()
                    .customerId(request.getCustomerId())
                    .contractId(request.getContractId())
                    .amount(request.getAmount())
                    .dueDate(request.getDueDate())
                    .description(request.getDescription())
                    .status("PENDING")
                    .createdAt(LocalDateTime.now())
                    .build();

            bill = billRepository.save(bill);
            log.info("Bill created successfully with ID: {}", bill.getId());

            // Kafka event göndermeyi devre dışı bırak
            // kafkaTemplate.send("billing-events", bill);

            return BillMapper.toResponse(bill);
        } catch (Exception e) {
            log.error("Error creating bill: {}", e.getMessage());
            throw new RuntimeException("Failed to create bill: " + e.getMessage());
        }
    }

    public BillResponse getBill(Long id) {
        return BillMapper.toResponse(findBillById(id));
    }

    public List<BillResponse> getBillsByCustomer(Long customerId) {
        return billRepository.findByCustomerId(customerId)
                .stream()
                .map(BillMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<BillResponse> getBillsByStatus(String status) {
        return billRepository.findByStatus(status)
                .stream()
                .map(BillMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BillResponse payBill(Long id) {
        Bill bill = findBillById(id);
        
        if (!"PENDING".equals(bill.getStatus())) {
            throw new BusinessException("Bill is not in PENDING status");
        }

        bill.setStatus("PAID");
        bill.setPaidAt(LocalDateTime.now());
        
        return BillMapper.toResponse(billRepository.save(bill));
    }

    @Transactional
    public BillResponse cancelBill(Long id) {
        Bill bill = findBillById(id);
        
        if (!"PENDING".equals(bill.getStatus())) {
            throw new BusinessException("Bill is not in PENDING status");
        }

        bill.setStatus("CANCELLED");
        
        return BillMapper.toResponse(billRepository.save(bill));
    }

    public List<BillResponse> getUnpaidBillsByCustomerId(Long customerId) {
        return billRepository.findByCustomerIdAndStatus(customerId, "PENDING")
                .stream()
                .map(BillMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<BillResponse> getBillsByDateRange(Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        return billRepository.findByCustomerIdAndCreatedAtBetween(customerId, startDate, endDate)
                .stream()
                .map(BillMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<BillResponse> getOverdueBills() {
        return billRepository.findByDueDateLessThanAndStatus(LocalDateTime.now(), "PENDING")
                .stream()
                .map(BillMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BillResponse updateBill(Long id, CreateBillRequest request) {
        Bill bill = findBillById(id);
        
        if (!"PENDING".equals(bill.getStatus())) {
            throw new BusinessException("Only PENDING bills can be updated");
        }

        // Test için Feign client kontrolünü bypass ediyoruz
        // CustomerDto customer = customerClient.getCustomerById(request.getCustomerId(), "Bearer test-token");
        // ContractResponse contract = contractClient.getContractById(request.getContractId());

        // if (customer == null) {
        //     throw new ResourceNotFoundException("Customer not found");
        // }
        // if (contract == null) {
        //     throw new ResourceNotFoundException("Contract not found");
        // }

        bill.setCustomerId(request.getCustomerId());
        bill.setContractId(request.getContractId());
        bill.setAmount(request.getAmount());
        bill.setDueDate(request.getDueDate());
        bill.setDescription(request.getDescription());
        
        return BillMapper.toResponse(billRepository.save(bill));
    }

    @Transactional
    public void deleteBill(Long id) {
        Bill bill = findBillById(id);
        
        if (!"PENDING".equals(bill.getStatus())) {
            throw new BusinessException("Only PENDING bills can be deleted");
        }

        billRepository.delete(bill);
    }

    private Bill findBillById(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
    }
}