package com.turkcell.billingservice.services;

import com.turkcell.billingservice.clients.CustomerServiceClient;
import com.turkcell.billingservice.domain.dtos.requests.BillRequest;
import com.turkcell.billingservice.domain.dtos.responses.BillResponse;
import com.turkcell.billingservice.domain.dtos.responses.CustomerResponse;
import com.turkcell.billingservice.domain.entities.Bill;
import com.turkcell.billingservice.domain.entities.BillItem;
import com.turkcell.billingservice.domain.enums.BillStatus;
import com.turkcell.billingservice.domain.enums.ItemType;
import com.turkcell.billingservice.domain.exceptions.BillingException;
import com.turkcell.billingservice.domain.mappers.BillMapper;
import com.turkcell.billingservice.repositories.BillRepository;
import com.turkcell.billingservice.validation.BillingValidator;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.math.BigDecimal;

/**
 * Fatura oluşturma ve güncelleme işlemlerini yöneten servis sınıfı.
 * Bu servis, faturaların oluşturulması, güncellenmesi ve silinmesi gibi
 * temel işlemleri gerçekleştirir. Ayrıca, fatura oluşturma sürecinde
 * vergi ve indirim hesaplamalarını da yapar.
 *
 * @author Turkcell
 * @since 1.0
 */
@Service
@Slf4j
public class BillCommandService {
    private static final double TAX_RATE = 0.20; // %20 KDV
    private static final double DISCOUNT_RATE = 0.10; // %10 indirim

    private final BillRepository billRepository;
    private final BillingValidator billingValidator;
    private final EventPublisher eventPublisher;
    private final NotificationHandler notificationHandler;
    private final CustomerServiceClient customerServiceClient;

    /**
     * Fatura işlemlerini yöneten servis sınıfı.
     * Fatura oluşturma, silme, güncelleme gibi işlemleri gerçekleştirir.
     * 
     * @param billRepository Fatura repository
     * @param billingValidator Fatura doğrulayıcı
     * @param eventPublisher Olay yayınlayıcı
     * @param notificationHandler Bildirim işleyici
     * @param customerServiceClient Müşteri servisi istemcisi
     */
    public BillCommandService(
            BillRepository billRepository,
            BillingValidator billingValidator,
            EventPublisher eventPublisher,
            NotificationHandler notificationHandler,
            CustomerServiceClient customerServiceClient) {
        this.billRepository = billRepository;
        this.billingValidator = billingValidator;
        this.eventPublisher = eventPublisher;
        this.notificationHandler = notificationHandler;
        this.customerServiceClient = customerServiceClient;
    }

    /**
     * Yeni bir fatura oluşturur.
     * Bu metot asenkron olarak çalışır ve fatura oluşturma sürecini yönetir.
     * Oluşturulan fatura için vergi ve indirim hesaplamaları yapılır,
     * müşteriye bildirim gönderilir ve ilgili olaylar yayınlanır.
     *
     * @param request Fatura oluşturma isteği
     * @return Oluşturulan fatura detayları
     * @throws BillingException Fatura oluşturma sırasında bir hata oluşursa
     */
    @Transactional
    @CircuitBreaker(name = "createBill", fallbackMethod = "createBillFallback")
    @Retry(name = "createBill")
    @TimeLimiter(name = "createBill")
    public CompletableFuture<BillResponse> createBill(BillRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Creating bill for customerId: {}", request.getCustomerId());
            
            try {
                // Request validasyonu
                billingValidator.validateBillRequest(request);
                
                // Müşteri validasyonu
                validateCustomer(request.getCustomerId()).join();
                
                // Fatura entity oluştur
                Bill entity = BillMapper.toEntity(request);
                entity.setStatus(BillStatus.PENDING);
                
                // Vergi ve indirim uygula
                applyTaxAndDiscount(entity);
                
                // Faturayı kaydet
                Bill savedEntity = billRepository.save(entity);
                log.info("Bill created with ID: {}", savedEntity.getId());
                
                // Müşteri bilgilerini al
                CustomerResponse customer = customerServiceClient.getCustomer(request.getCustomerId());
                
                // Olayları yayınla
                eventPublisher.publishBillCreatedEvent(savedEntity);
                
                // Bildirim gönder
                notificationHandler.sendBillCreatedNotification(savedEntity, customer);
                
                return BillMapper.toResponse(savedEntity);
            } catch (Exception e) {
                throw new BillingException("Failed to create bill: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Toplu fatura oluşturur.
     * Birden fazla faturayı tek seferde oluşturmak için kullanılır.
     * Her fatura için ayrı ayrı vergi ve indirim hesaplamaları yapılır.
     *
     * @param requests Fatura oluşturma istekleri listesi
     * @return Oluşturulan faturaların detayları
     * @throws BillingException Toplu fatura oluşturma sırasında bir hata oluşursa
     */
    @Transactional
    public List<BillResponse> createBillsBatch(List<BillRequest> requests) {
        log.info("Creating batch bills, count: {}", requests.size());
        
        try {
            List<BillResponse> responses = requests.stream().map(request -> {
                try {
                    return createBill(request).join();
                } catch (Exception e) {
                    log.error("Error creating bill for customerId: {}, error: {}", request.getCustomerId(), e.getMessage());
                    return null;
                }
            }).filter(bill -> bill != null).collect(Collectors.toList());
            
            return responses;
        } catch (Exception e) {
            throw new BillingException("Failed to create batch bills: " + e.getMessage(), e);
        }
    }

    /**
     * Fatura durumunu günceller.
     * Belirtilen fatura ID'sine sahip faturanın durumunu yeni durum ile günceller.
     *
     * @param billId Fatura ID'si
     * @param status Yeni durum
     * @throws BillingException Fatura bulunamazsa veya geçersiz durum belirtilirse
     */
    @Transactional
    public void updateBillStatus(UUID billId, String status) {
        log.info("Updating bill status for billId: {}, status: {}", billId, status);
        
        BillStatus billStatus;
        try {
            billStatus = BillStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BillingException("Invalid bill status: " + status);
        }
        
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BillingException("Bill not found"));
        bill.setStatus(billStatus);
        bill.setUpdatedAt(LocalDateTime.now());
        
        billRepository.save(bill);
        log.info("Bill status updated for billId: {}", billId);
    }

    /**
     * Faturayı iptal eder.
     *
     * @param billId Fatura ID'si
     * @return Güncellenen fatura
     */
    @Transactional
    public BillResponse cancelBill(UUID billId) {
        log.info("Cancelling bill with ID: {}", billId);
        
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BillingException("Bill not found"));
        
        if (bill.getStatus() == BillStatus.PAID) {
            throw new BillingException("Cannot cancel a paid bill");
        }
        
        bill.setStatus(BillStatus.CANCELLED);
        bill.setUpdatedAt(LocalDateTime.now());
        
        Bill savedBill = billRepository.save(bill);
        log.info("Bill cancelled successfully with ID: {}", billId);
        
        return BillMapper.toResponse(savedBill);
    }

    /**
     * Faturayı siler.
     * Belirtilen fatura ID'sine sahip faturayı sistemden siler.
     *
     * @param billId Fatura ID'si
     * @throws BillingException Fatura bulunamazsa
     */
    @Transactional
    public void deleteBill(UUID billId) {
        log.info("Deleting bill with ID: {}", billId);
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BillingException("Bill not found"));
        
        billRepository.delete(bill);
        log.info("Bill deleted with ID: {}", billId);
    }

    /**
     * Müşteri validasyonu yapar.
     *
     * @param customerId Müşteri ID'si
     * @return Validasyon sonucu
     */
    @CircuitBreaker(name = "customerService", fallbackMethod = "customerServiceFallback")
    @Retry(name = "customerService")
    @TimeLimiter(name = "customerService")
    private CompletableFuture<Boolean> validateCustomer(UUID customerId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Validating customer with ID: {}", customerId);
            boolean isValid = customerServiceClient.validateCustomer(customerId);
            if (!isValid) {
                throw new BillingException("Customer validation failed");
            }
            return true;
        });
    }

    /**
     * Vergi ve indirim uygular.
     *
     * @param entity Fatura entity
     */
    private void applyTaxAndDiscount(Bill entity) {
        BigDecimal subtotal = entity.getItems().stream()
                .filter(item -> item.getItemType() == ItemType.FIXED_FEE || item.getItemType() == ItemType.EXTRA_PACKAGE_FEE)
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Vergi hesaplama
        BigDecimal taxRate = new BigDecimal(TAX_RATE);
        BigDecimal taxAmount = subtotal.multiply(taxRate);
        BillItem taxItem = BillItem.builder()
                .bill(entity)
                .description("KDV (%20)")
                .amount(taxAmount)
                .itemType(ItemType.TAX)
                .quantity(1)
                .build();
        entity.getItems().add(taxItem);

        // İndirim hesaplama
        BigDecimal discountRate = new BigDecimal(DISCOUNT_RATE);
        BigDecimal discountAmount = subtotal.multiply(discountRate);
        BillItem discountItem = BillItem.builder()
                .bill(entity)
                .description("İndirim (%10)")
                .amount(discountAmount.negate()) // Negatif değer olarak eklenir
                .itemType(ItemType.DISCOUNT)
                .quantity(1)
                .build();
        entity.getItems().add(discountItem);

        // Toplam tutarı güncelle
        BigDecimal totalAmount = subtotal.add(taxAmount).subtract(discountAmount);
        entity.setTotalAmount(totalAmount.doubleValue());
    }

    /**
     * Müşteri servisi fallback metodu.
     *
     * @param customerId Müşteri ID'si
     * @param t Hata
     * @return Validasyon sonucu
     */
    private CompletableFuture<Boolean> customerServiceFallback(UUID customerId, Throwable t) {
        log.error("Fallback for validateCustomer, id: {}, error: {}", customerId, t.getMessage());
        
        return CompletableFuture.failedFuture(
            new BillingException("Customer service is not available", t)
        );
    }

    /**
     * Fatura oluşturma fallback metodu.
     *
     * @param request Fatura isteği
     * @param t Hata
     * @return Fatura sonucu
     */
    private CompletableFuture<BillResponse> createBillFallback(BillRequest request, Throwable t) {
        log.error("Fallback for createBill, customerId: {}, error: {}", request.getCustomerId(), t.getMessage());
        
        return CompletableFuture.failedFuture(
            new BillingException("Failed to create bill: " + t.getMessage(), t)
        );
    }

    /**
     * Vadesi yaklaşan veya geçmiş faturalar için hatırlatma gönderir.
     * Bu metot genellikle bir scheduler tarafından periyodik olarak çağrılır.
     */
    @Transactional
    public void sendBillReminders() {
        log.info("Starting bill reminder process");
        
        // 1. Vadesi yaklaşan faturaları bul (3 gün içinde vadesi dolacak)
        LocalDate now = LocalDate.now();
        LocalDate threeDaysFromNow = now.plusDays(3);
        List<Bill> upcomingBills = billRepository.findByDueDateBetweenAndStatus(
            now, 
            threeDaysFromNow, 
            BillStatus.PENDING
        );
        
        // 2. Vadesi geçmiş faturaları bul
        List<Bill> overdueBills = billRepository.findByDueDateBeforeAndStatus(
            now, 
            BillStatus.PENDING
        );
        
        // 3. Hatırlatma gönder
        for (Bill bill : upcomingBills) {
            try {
                CustomerResponse customer = customerServiceClient.getCustomer(bill.getCustomerId());
                notificationHandler.sendUpcomingBillReminder(bill, customer);
                log.info("Upcoming bill reminder sent for billId: {}, customerId: {}", 
                        bill.getId(), bill.getCustomerId());
            } catch (Exception e) {
                log.error("Error sending upcoming bill reminder for billId: {}: {}", 
                        bill.getId(), e.getMessage());
            }
        }
        
        for (Bill bill : overdueBills) {
            try {
                // Fatura durumunu güncelle
                bill.setStatus(BillStatus.OVERDUE);
                bill.setUpdatedAt(LocalDateTime.now());
                billRepository.save(bill);
                
                CustomerResponse customer = customerServiceClient.getCustomer(bill.getCustomerId());
                notificationHandler.sendOverdueBillReminder(bill, customer);
                log.info("Overdue bill reminder sent for billId: {}, customerId: {}", 
                        bill.getId(), bill.getCustomerId());
            } catch (Exception e) {
                log.error("Error sending overdue bill reminder for billId: {}: {}", 
                        bill.getId(), e.getMessage());
            }
        }
        
        log.info("Bill reminder process completed");
    }
} 