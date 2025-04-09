package com.turkcell.billingservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingReportResponse {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<BillResponse> bills;
    private List<PaymentResponse> payments;
    private List<RefundResponse> refunds;
    private Double totalBilledAmount;
    private Double totalPaidAmount;
    private Double totalRefundedAmount;
    private Integer totalBillCount;
    private Integer totalPaymentCount;
    private Integer totalRefundCount;
} 