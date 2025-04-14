package com.turkcell.billingservice.validation;

import com.turkcell.billingservice.domain.dtos.requests.BillRequest;
import com.turkcell.billingservice.domain.entities.Bill;
import com.turkcell.billingservice.domain.entities.Payment;
import com.turkcell.billingservice.domain.enums.BillStatus;
import com.turkcell.billingservice.exceptions.BillingException;
import com.turkcell.billingservice.exceptions.ResourceNotFoundException;
import com.turkcell.billingservice.repositories.BillRepository;
import com.turkcell.billingservice.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Fatura ve ödeme işlemleri için gerekli validasyon kurallarını içeren sınıf.
 * İş kurallarının doğrulanması için kullanılır.
 */
@Component
@RequiredArgsConstructor
public class BillingValidator {
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    
    /**
     * Fatura isteğinin geçerli olup olmadığını kontrol eder
     * @param request Kontrol edilecek fatura isteği
     * @throws BillingException İstek geçersiz ise fırlatılır
     */
    public void validateBillRequest(BillRequest request) {
        if (request == null) {
            throw new BillingException("Bill request cannot be null");
        }
        
        if (request.getCustomerId() == null) {
            throw new BillingException("Customer ID cannot be null");
        }
        
        if (request.getContractId() == null) {
            throw new BillingException("Contract ID cannot be null");
        }
        
        if (request.getTotalAmount() == null || request.getTotalAmount() <= 0) {
            throw new BillingException("Total amount must be greater than zero");
        }
        
        if (request.getDueDate() == null) {
            throw new BillingException("Due date cannot be null");
        }
        
        if (request.getDueDate().isBefore(LocalDate.now())) {
            throw new BillingException("Due date cannot be in the past");
        }
        
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BillingException("Bill must contain at least one item");
        }
        
        // Her bir öğenin geçerliliğini kontrol et
        request.getItems().forEach(item -> {
            if (item.getDescription() == null || item.getDescription().trim().isEmpty()) {
                throw new BillingException("Item description cannot be empty");
            }
            
            if (item.getAmount() == null || item.getAmount() <= 0) {
                throw new BillingException("Item amount must be greater than zero");
            }
            
            if (item.getItemType() == null) {
                throw new BillingException("Item type cannot be null");
            }
        });
    }
    
    /**
     * Verilen ID'ye sahip fatura var mı kontrol eder
     * @param billId Kontrol edilecek fatura ID'si
     * @return Varsa faturayı döner
     * @throws BillingException Fatura bulunamazsa fırlatılır
     */
    public Bill validateBillExists(UUID billId) {
        return billRepository.findById(billId)
                .orElseThrow(() -> new BillingException("Bill not found with id: " + billId));
    }
    
    /**
     * Verilen ID'ye sahip ödeme var mı kontrol eder
     * @param paymentId Kontrol edilecek ödeme ID'si
     * @return Varsa ödemeyi döner
     * @throws BillingException Ödeme bulunamazsa fırlatılır
     */
    public Payment validatePaymentExists(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BillingException("Payment not found with id: " + paymentId));
    }
    
    /**
     * Faturanın ödenebilir durumda olup olmadığını kontrol eder
     * @param bill Kontrol edilecek fatura
     * @throws BillingException Fatura ödenemez durumda ise fırlatılır
     */
    public void validateBillCanBePaid(Bill bill) {
        if (bill.getStatus() == BillStatus.PAID) {
            throw new BillingException("Bill is already paid");
        }
        
        if (bill.getStatus() == BillStatus.CANCELLED) {
            throw new BillingException("Bill is cancelled and cannot be paid");
        }
    }
    
    /**
     * Ödeme miktarının geçerli olup olmadığını kontrol eder
     * @param amount Kontrol edilecek miktar
     * @param billAmount Fatura tutarı
     * @throws BillingException Miktar geçersiz ise fırlatılır
     */
    public void validatePaymentAmount(Double amount, Double billAmount) {
        if (amount == null || amount <= 0) {
            throw new BillingException("Payment amount must be greater than zero");
        }
        
        if (amount < billAmount) {
            throw new BillingException("Payment amount must be at least equal to bill amount");
        }
    }
    
    /**
     * Müşteri ID'sinin geçerli olup olmadığını kontrol eder
     * @param customerId Kontrol edilecek müşteri ID'si
     * @throws BillingException ID geçersiz ise fırlatılır
     */
    public void validateCustomerId(UUID customerId) {
        if (customerId == null) {
            throw new BillingException("Customer ID cannot be null");
        }
    }
    
    /**
     * Faturanın iptal edilebilir durumda olup olmadığını kontrol eder
     * @param bill Kontrol edilecek fatura
     * @throws BillingException Fatura iptal edilemez durumda ise fırlatılır
     */
    public void validateBillCanBeCancelled(Bill bill) {
        if (bill.getStatus() == BillStatus.CANCELLED) {
            throw new BillingException("Bill is already cancelled");
        }
        
        if (bill.getStatus() == BillStatus.PAID) {
            throw new BillingException("Paid bills cannot be cancelled");
        }
    }
} 