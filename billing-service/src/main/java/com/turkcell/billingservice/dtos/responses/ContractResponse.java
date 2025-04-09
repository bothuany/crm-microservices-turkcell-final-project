package com.turkcell.billingservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractResponse {
    private Long id;
    private Long customerId;
    private String serviceType;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double monthlyFee;
    private String description;
} 