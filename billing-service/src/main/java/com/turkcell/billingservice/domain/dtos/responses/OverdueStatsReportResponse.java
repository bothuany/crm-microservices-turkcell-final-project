package com.turkcell.billingservice.domain.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverdueStatsReportResponse {
    private UUID customerId;
    private LocalDate startDate;
    private LocalDate endDate;
    private double overduePercentage;
    private double totalBilled;
    private double totalOverdue;
} 