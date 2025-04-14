package com.turkcell.billingservice.services;

import com.turkcell.billingservice.domain.dtos.responses.BillResponse;
import com.turkcell.billingservice.domain.entities.Bill;
import com.turkcell.billingservice.domain.enums.BillStatus;
import com.turkcell.billingservice.domain.exceptions.BillingException;
import com.turkcell.billingservice.domain.mappers.BillMapper;
import com.turkcell.billingservice.repositories.BillRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Fatura sorgulama işlemlerini yönetir.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillQueryService {
    private final BillRepository billRepository;

    /**
     * Fatura detaylarını getirir.
     *
     * @param billId Fatura ID'si
     * @return Fatura detayları
     */
    @Cacheable(value = "bills", key = "#billId")
    @CircuitBreaker(name = "getBill", fallbackMethod = "getBillFallback")
    public BillResponse getBill(UUID billId) {
        log.info("Fetching bill with ID: {}", billId);
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BillingException("Bill not found with id: " + billId));
        return BillMapper.toResponse(bill);
    }

    /**
     * Tüm faturaları sayfalı olarak getirir.
     *
     * @param pageable Sayfalama bilgisi
     * @return Faturalar listesi
     */
    public Page<BillResponse> getAllBills(Pageable pageable) {
        log.info("Fetching all bills with pagination");
        Page<Bill> bills = billRepository.findAll(pageable);
        return bills.map(BillMapper::toResponse);
    }

    /**
     * Müşteriye göre faturaları getirir.
     *
     * @param customerId Müşteri ID'si
     * @param pageable Sayfalama bilgisi
     * @return Faturalar listesi
     */
    public Page<BillResponse> getBillsByCustomer(UUID customerId, Pageable pageable) {
        log.info("Fetching bills for customer ID: {}", customerId);
        Page<Bill> bills = billRepository.findByCustomerId(customerId, pageable);
        return bills.map(BillMapper::toResponse);
    }

    /**
     * Duruma göre faturaları getirir.
     *
     * @param status Durum
     * @param pageable Sayfalama bilgisi
     * @return Faturalar listesi
     */
    public Page<BillResponse> getBillsByStatus(String status, Pageable pageable) {
        log.info("Fetching bills with status: {}", status);
        BillStatus billStatus = BillStatus.fromString(status);
        Page<Bill> bills = billRepository.findByStatus(billStatus, pageable);
        return bills.map(BillMapper::toResponse);
    }

    /**
     * Müşteri ve tarih aralığına göre faturaları getirir.
     *
     * @param customerId Müşteri ID'si
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @param pageable Sayfalama bilgisi
     * @return Faturalar listesi
     */
    public Page<BillResponse> getBillsByCustomerAndDateRange(UUID customerId, String startDate, String endDate, Pageable pageable) {
        log.info("Fetching bills for customer ID: {} between {} and {}", customerId, startDate, endDate);
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        Page<Bill> bills = billRepository.findByCustomerIdAndDueDateBetween(customerId, start, end, pageable);
        return bills.map(BillMapper::toResponse);
    }

    /**
     * Müşteriye ait ödenmemiş faturaları getirir.
     *
     * @param customerId Müşteri ID'si
     * @param pageable Sayfalama bilgisi
     * @return Ödenmemiş faturalar listesi
     */
    public Page<BillResponse> getUnpaidBills(UUID customerId, Pageable pageable) {
        log.info("Fetching unpaid bills for customer ID: {}", customerId);
        Page<Bill> bills = billRepository.findByCustomerIdAndStatus(customerId, BillStatus.PENDING, pageable);
        return bills.map(BillMapper::toResponse);
    }

    /**
     * Müşteriye ait gecikmiş faturaları getirir.
     *
     * @param customerId Müşteri ID'si
     * @param pageable Sayfalama bilgisi
     * @return Gecikmiş faturalar listesi
     */
    public Page<BillResponse> getOverdueBills(UUID customerId, Pageable pageable) {
        log.info("Fetching overdue bills for customer ID: {}", customerId);
        Page<Bill> bills = billRepository.findByCustomerIdAndStatus(customerId, BillStatus.OVERDUE, pageable);
        return bills.map(BillMapper::toResponse);
    }

    /**
     * Faturanın belirtilen müşteriye ait olup olmadığını kontrol eder.
     *
     * @param billId Fatura ID'si
     * @param customerId Müşteri ID'si
     * @return Faturanın müşteriye ait olup olmadığı
     */
    public boolean isBillBelongsToCustomer(UUID billId, String customerId) {
        log.info("Checking if bill {} belongs to customer {}", billId, customerId);
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BillingException("Bill not found with id: " + billId));
        return bill.getCustomerId().toString().equals(customerId);
    }

    /**
     * Fatura getirme fallback metodu.
     *
     * @param billId Fatura ID'si
     * @param t Hata
     * @return Fatura detayları
     */
    private BillResponse getBillFallback(UUID billId, Throwable t) {
        log.error("Fallback for getBill, id: {}, error: {}", billId, t.getMessage());
        throw new BillingException("Failed to get bill: " + t.getMessage(), t);
    }
} 