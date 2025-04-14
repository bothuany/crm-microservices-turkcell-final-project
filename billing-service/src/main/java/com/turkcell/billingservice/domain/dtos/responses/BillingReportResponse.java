package com.turkcell.billingservice.domain.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Faturalandırma raporlarının sonuç DTO'su
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingReportResponse {
    /**
     * Rapor başlangıç tarihi
     */
    private LocalDate startDate;
    
    /**
     * Rapor bitiş tarihi
     */
    private LocalDate endDate;
    
    /**
     * Müşteri ID
     */
    private UUID customerId;
    
    /**
     * Toplam fatura sayısı
     */
    private int totalBillCount;
    
    /**
     * Toplam ödeme sayısı
     */
    private int totalPaymentCount;
    
    /**
     * Toplam ödeme miktarı
     */
    private double totalPaymentAmount;
    
    /**
     * Toplam iade sayısı
     */
    private int totalRefundCount;
    
    /**
     * Toplam iade miktarı
     */
    private double totalRefundAmount;
    
    /**
     * Fatura durumlarına göre dağılım
     */
    private Map<String, Integer> billStatusDistribution;
    
    /**
     * Ödeme yöntemlerine göre dağılım
     */
    private Map<String, Integer> paymentMethodDistribution;
    
    /**
     * Aylara göre fatura tutarları
     */
    private Map<String, Double> monthlyBillingAmount;
    
    /**
     * Ödenmeyen faturalar
     */
    private List<BillResponse> unpaidBills;
}