package com.turkcell.billingservice.utils;

import com.turkcell.billingservice.dtos.responses.BillResponse;
import com.turkcell.billingservice.entities.Bill;

public class BillMapper {
    public static BillResponse toResponse(Bill bill) {
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