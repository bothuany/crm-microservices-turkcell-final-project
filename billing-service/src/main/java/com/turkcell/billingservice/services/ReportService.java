package com.turkcell.billingservice.services;

import com.turkcell.billingservice.domain.dtos.responses.BillingReportResponse;
import com.turkcell.billingservice.domain.entities.Bill;
import com.turkcell.billingservice.domain.entities.Payment;
import com.turkcell.billingservice.domain.entities.Refund;
import com.turkcell.billingservice.domain.enums.BillStatus;
import com.turkcell.billingservice.domain.mappers.BillMapper;
import com.turkcell.billingservice.repositories.BillRepository;
import com.turkcell.billingservice.repositories.PaymentRepository;
import com.turkcell.billingservice.repositories.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Faturalandırma ile ilgili raporları oluşturan servis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    
    /**
     * Belirli bir müşteri için faturalandırma raporu oluşturur.
     *
     * @param customerId Müşteri ID'si
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return Faturalandırma raporu
     */
    @Cacheable(value = "billingReports", key = "#customerId + '-' + #startDate + '-' + #endDate")
    public BillingReportResponse generateBillingReport(UUID customerId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating billing report for customer ID: {} from {} to {}", customerId, startDate, endDate);
        
        // Belirtilen tarih aralığındaki faturaları getir
        List<Bill> bills = billRepository.findByCustomerIdAndDueDateBetween(customerId, startDate, endDate);
        
        // Faturaların ID'lerini topla
        List<UUID> billIds = bills.stream()
                .map(Bill::getId)
                .collect(Collectors.toList());
        
        // Bu faturalara ait ödemeleri getir
        List<Payment> payments = paymentRepository.findByBillIdIn(billIds);
        
        // Ödemelerin ID'lerini topla
        List<UUID> paymentIds = payments.stream()
                .map(Payment::getId)
                .collect(Collectors.toList());
        
        // Bu ödemelere ait iadeleri getir
        List<Refund> refunds = refundRepository.findByPaymentIdIn(paymentIds);
        
        // Fatura durumlarına göre dağılımı hesapla
        Map<String, Integer> billStatusDistribution = calculateBillStatusDistribution(bills);
        
        // Ödeme yöntemlerine göre dağılımı hesapla
        Map<String, Integer> paymentMethodDistribution = calculatePaymentMethodDistribution(payments);
        
        // Aylara göre fatura tutarlarını hesapla
        Map<String, Double> monthlyBillingAmount = calculateMonthlyBillingAmount(bills);
        
        // Ödenmeyen faturaları getir
        List<Bill> unpaidBills = bills.stream()
                .filter(bill -> bill.getStatus() == BillStatus.PENDING || bill.getStatus() == BillStatus.OVERDUE)
                .collect(Collectors.toList());
        
        // Toplam tutarları hesapla
        double totalPaymentAmount = payments.stream()
                .mapToDouble(payment -> payment.getAmount().doubleValue())
                .sum();
        
        double totalRefundAmount = refunds.stream()
                .mapToDouble(refund -> refund.getAmount().doubleValue())
                .sum();
        
        // Raporu oluştur
        return BillingReportResponse.builder()
                .customerId(customerId)
                .startDate(startDate)
                .endDate(endDate)
                .totalBillCount(bills.size())
                .totalPaymentCount(payments.size())
                .totalPaymentAmount(totalPaymentAmount)
                .totalRefundCount(refunds.size())
                .totalRefundAmount(totalRefundAmount)
                .billStatusDistribution(billStatusDistribution)
                .paymentMethodDistribution(paymentMethodDistribution)
                .monthlyBillingAmount(monthlyBillingAmount)
                .unpaidBills(unpaidBills.stream().map(BillMapper::toResponse).collect(Collectors.toList()))
                .build();
    }
    
    /**
     * Fatura durumlarına göre dağılımı hesaplar.
     *
     * @param bills Faturalar listesi
     * @return Durumlara göre dağılım haritası
     */
    private Map<String, Integer> calculateBillStatusDistribution(List<Bill> bills) {
        Map<String, Integer> distribution = new HashMap<>();
        bills.forEach(bill -> {
            String status = bill.getStatus().toString();
            distribution.put(status, distribution.getOrDefault(status, 0) + 1);
        });
        return distribution;
    }
    
    /**
     * Ödeme yöntemlerine göre dağılımı hesaplar.
     *
     * @param payments Ödemeler listesi
     * @return Ödeme yöntemlerine göre dağılım haritası
     */
    private Map<String, Integer> calculatePaymentMethodDistribution(List<Payment> payments) {
        Map<String, Integer> distribution = new HashMap<>();
        payments.forEach(payment -> {
            String method = payment.getPaymentMethod().toString();
            distribution.put(method, distribution.getOrDefault(method, 0) + 1);
        });
        return distribution;
    }
    
    /**
     * Aylara göre fatura tutarlarını hesaplar.
     *
     * @param bills Faturalar listesi
     * @return Aylara göre tutar haritası
     */
    private Map<String, Double> calculateMonthlyBillingAmount(List<Bill> bills) {
        Map<String, Double> monthlyAmount = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        bills.forEach(bill -> {
            String month = bill.getDueDate().format(formatter);
            monthlyAmount.put(month, monthlyAmount.getOrDefault(month, 0.0) + bill.getTotalAmount());
        });
        return monthlyAmount;
    }
} 