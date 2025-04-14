package com.turkcell.billingservice.domain.mappers;

import com.turkcell.billingservice.domain.entities.Bill;
import com.turkcell.billingservice.domain.entities.BillItem;
import com.turkcell.billingservice.domain.dtos.requests.BillRequest;
import com.turkcell.billingservice.domain.dtos.requests.BillItemRequest;
import com.turkcell.billingservice.domain.dtos.responses.BillResponse;
import com.turkcell.billingservice.domain.dtos.responses.BillItemResponse;
import com.turkcell.billingservice.domain.enums.BillStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bill entity ve DTO'ları arasında dönüşüm yapar.
 */
@Component
public class BillMapper {

    /**
     * BillRequest'i Bill entity'ye dönüştürür.
     *
     * @param request BillRequest
     * @return Bill entity
     */
    public static Bill toEntity(BillRequest request) {
        Bill bill = Bill.builder()
                .customerId(request.getCustomerId())
                .contractId(request.getContractId())
                .totalAmount(request.getTotalAmount())
                .dueDate(request.getDueDate())
                .description(request.getDescription())
                .status(BillStatus.PENDING)
                .items(new ArrayList<>())
                .build();
        
        // bill_items ekleniyor
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (BillItemRequest itemRequest : request.getItems()) {
                BillItem item = BillItem.builder()
                        .bill(bill)
                        .itemType(itemRequest.getItemType())
                        .description(itemRequest.getDescription())
                        .amount(BigDecimal.valueOf(itemRequest.getAmount()))
                        .quantity(1) // Varsayılan olarak 1 miktar ayarlandı
                        .build();
                bill.getItems().add(item);
            }
        }
        
        return bill;
    }

    /**
     * Bill entity'yi BillResponse'a dönüştürür.
     *
     * @param bill Bill entity
     * @return BillResponse
     */
    public static BillResponse toResponse(Bill bill) {
        if (bill == null) {
            return null;
        }
        
        return BillResponse.builder()
                .id(bill.getId())
                .customerId(bill.getCustomerId())
                .contractId(bill.getContractId())
                .totalAmount(bill.getTotalAmount())
                .dueDate(bill.getDueDate())
                .status(bill.getStatus())
                .paidAt(bill.getPaidAt())
                .description(bill.getDescription())
                .createdAt(bill.getCreatedAt())
                .updatedAt(bill.getUpdatedAt())
                .items(bill.getItems() != null ?
                        bill.getItems().stream()
                                .map(BillMapper::toItemResponse)
                                .collect(Collectors.toList()) : null)
                .build();
    }

    /**
     * BillItem entity'yi BillItemResponse'a dönüştürür.
     *
     * @param item BillItem entity
     * @return BillItemResponse
     */
    public static BillItemResponse toItemResponse(BillItem item) {
        if (item == null) {
            return null;
        }
        
        return BillItemResponse.builder()
                .id(item.getId())
                .billId(item.getBillId())
                .itemType(item.getItemType())
                .description(item.getDescription())
                .amount(item.getAmount())
                .quantity(item.getQuantity())
                .createdAt(item.getCreatedAt())
                .build();
    }
} 