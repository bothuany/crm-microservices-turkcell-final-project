package com.turkcell.billingservice.services;

import com.turkcell.billingservice.dtos.responses.BillingReportResponse;
import com.turkcell.billingservice.dtos.responses.BillResponse;
import com.turkcell.billingservice.dtos.responses.PaymentResponse;
import com.turkcell.billingservice.dtos.responses.RefundResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final BillService billService;
    private final PaymentService paymentService;
    private final RefundService refundService;

    public BillingReportResponse generateBillingReport(Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        // Fatura, ödeme ve iade bilgilerini al
        List<BillResponse> bills = billService.getBillsByCustomerAndDateRange(customerId, startDate, endDate);
        List<PaymentResponse> payments = paymentService.getPaymentsByCustomer(customerId);
        List<RefundResponse> refunds = refundService.getRefundsByCustomer(customerId);

        // Toplam tutarları hesapla
        double totalBilledAmount = bills.stream()
                .mapToDouble(BillResponse::getAmount)
                .sum();

        double totalPaidAmount = payments.stream()
                .filter(payment -> payment.getStatus().equals("SUCCESS"))
                .mapToDouble(PaymentResponse::getAmount)
                .sum();

        double totalRefundedAmount = refunds.stream()
                .filter(refund -> refund.getStatus().equals("COMPLETED"))
                .mapToDouble(RefundResponse::getAmount)
                .sum();

        // Raporu oluştur
        return BillingReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .bills(bills)
                .payments(payments)
                .refunds(refunds)
                .totalBilledAmount(totalBilledAmount)
                .totalPaidAmount(totalPaidAmount)
                .totalRefundedAmount(totalRefundedAmount)
                .totalBillCount(bills.size())
                .totalPaymentCount(payments.size())
                .totalRefundCount(refunds.size())
                .build();
    }
} 