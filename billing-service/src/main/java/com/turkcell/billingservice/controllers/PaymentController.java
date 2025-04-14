package com.turkcell.billingservice.controllers;

import com.turkcell.billingservice.domain.dtos.requests.PaymentRequest;
import com.turkcell.billingservice.domain.dtos.requests.RefundRequest;
import com.turkcell.billingservice.domain.dtos.responses.PaymentResponse;
import com.turkcell.billingservice.domain.dtos.responses.RefundResponse;
import com.turkcell.billingservice.services.PaymentCommandService;
import com.turkcell.billingservice.services.PaymentQueryService;
import io.github.bucket4j.Bucket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Ödeme ve iade işlemleri için REST API endpoint'lerini sağlayan controller.
 */
@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment Controller", description = "Ödeme ve iade işlemleri için API endpoint'leri")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final PaymentCommandService commandService;
    private final PaymentQueryService queryService;
    private final Bucket defaultBucket;

    /**
     * Yeni bir ödeme işlemi oluşturur.
     *
     * @param request Ödeme detayları
     * @return Oluşturulan ödeme bilgileri
     */
    @PostMapping
    @Operation(summary = "Yeni ödeme oluştur", description = "Belirtilen fatura için yeni bir ödeme işlemi oluşturur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ödeme başarıyla gerçekleştirildi"),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek verisi"),
            @ApiResponse(responseCode = "403", description = "Yetkilendirme hatası"),
            @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #request.customerId.toString() == authentication.principal.username)")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        if (!defaultBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        log.info("Processing payment for billId: {}, customerId: {}", request.getBillId(), request.getCustomerId());
        PaymentResponse response = commandService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Belirtilen ID'ye sahip ödemeyi getirir.
     *
     * @param paymentId Ödeme ID'si
     * @return Ödeme detayları
     */
    @GetMapping("/payment/{paymentId}")
    @Operation(summary = "Ödeme detayı getir", description = "Belirtilen ID'ye sahip ödemenin detaylarını getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ödeme başarıyla getirildi"),
            @ApiResponse(responseCode = "404", description = "Ödeme bulunamadı"),
            @ApiResponse(responseCode = "403", description = "Yetkilendirme hatası"),
            @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @paymentQueryService.isPaymentBelongsToCustomer(#paymentId, authentication.principal.username))")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        if (!defaultBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        log.info("Fetching payment with ID: {}", paymentId);
        PaymentResponse response = queryService.getPayment(paymentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Belirtilen faturaya ait ödemeleri getirir.
     *
     * @param billId Fatura ID'si
     * @return Faturaya ait ödemeler
     */
    @GetMapping("/bill/{billId}")
    @Operation(summary = "Fatura ödemelerini getir", description = "Belirtilen faturaya ait tüm ödemeleri getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ödemeler başarıyla getirildi"),
            @ApiResponse(responseCode = "403", description = "Yetkilendirme hatası"),
            @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @billQueryService.isBillBelongsToCustomer(#billId, authentication.principal.username))")
    public ResponseEntity<Page<PaymentResponse>> getPaymentsByBill(@PathVariable UUID billId, Pageable pageable) {
        if (!defaultBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        log.info("Fetching payments for bill ID: {}", billId);
        Page<PaymentResponse> payments = queryService.getPaymentsByBill(billId, pageable);
        return ResponseEntity.ok(payments);
    }

    /**
     * Belirtilen müşteriye ait ödemeleri getirir.
     *
     * @param customerId Müşteri ID'si
     * @return Müşteriye ait ödemeler
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Müşteriye göre ödemeleri getir", description = "Belirtilen müşteriye ait ödemeleri sayfalı olarak getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ödemeler başarıyla getirildi"),
            @ApiResponse(responseCode = "403", description = "Yetkilendirme hatası"),
            @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #customerId.toString() == authentication.principal.username)")
    public ResponseEntity<Page<PaymentResponse>> getPaymentsByCustomer(@PathVariable UUID customerId, Pageable pageable) {
        if (!defaultBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        log.info("Fetching payments for customer ID: {}", customerId);
        Page<PaymentResponse> payments = queryService.getPaymentsByCustomer(customerId, pageable);
        return ResponseEntity.ok(payments);
    }

    /**
     * Belirtilen ödemeyi siler.
     *
     * @param paymentId Silinecek ödeme ID'si
     * @return İşlem sonucu
     */
    @DeleteMapping("/payment/{paymentId}")
    @Operation(summary = "Ödeme sil", description = "Belirtilen ID'ye sahip ödemeyi siler")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ödeme başarıyla silindi"),
            @ApiResponse(responseCode = "404", description = "Ödeme bulunamadı"),
            @ApiResponse(responseCode = "403", description = "Yetkilendirme hatası"),
            @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @paymentQueryService.isPaymentBelongsToCustomer(#paymentId, authentication.principal.username))")
    public ResponseEntity<Void> deletePayment(@PathVariable UUID paymentId) {
        if (!defaultBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        log.info("Deleting payment with ID: {}", paymentId);
        commandService.deletePayment(paymentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Yeni bir iade işlemi oluşturur.
     *
     * @param request İade detayları
     * @return Oluşturulan iade bilgileri
     */
    @PostMapping("/refunds")
    @Operation(summary = "İade oluştur", description = "Belirtilen ödeme için yeni bir iade işlemi oluşturur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "İade başarıyla oluşturuldu"),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek verisi"),
            @ApiResponse(responseCode = "403", description = "Yetkilendirme hatası"),
            @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @paymentQueryService.isPaymentBelongsToCustomer(#request.paymentId, authentication.principal.username))")
    public ResponseEntity<RefundResponse> processRefund(@Valid @RequestBody RefundRequest request) {
        if (!defaultBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        log.info("Processing refund for paymentId: {}, amount: {}", request.getPaymentId(), request.getAmount());
        return ResponseEntity.ok(commandService.processRefund(request));
    }

    /**
     * Belirtilen ödemeye ait iadeleri getirir.
     *
     * @param paymentId Ödeme ID'si
     * @return Ödemeye ait iadeler
     */
    @GetMapping("/payment/{paymentId}/refunds")
    @Operation(summary = "Ödeme iadelerini getir", description = "Belirtilen ödemeye ait tüm iadeleri getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "İadeler başarıyla getirildi"),
            @ApiResponse(responseCode = "403", description = "Yetkilendirme hatası"),
            @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @paymentQueryService.isPaymentBelongsToCustomer(#paymentId, authentication.principal.username))")
    public ResponseEntity<List<RefundResponse>> getRefundsByPayment(@PathVariable UUID paymentId) {
        if (!defaultBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        log.info("Fetching refunds for payment ID: {}", paymentId);
        return ResponseEntity.ok(commandService.getRefundsByPayment(paymentId));
    }

    /**
     * Belirtilen faturaya ait iadeleri getirir.
     *
     * @param billId Fatura ID'si
     * @return Faturaya ait iadeler
     */
    @GetMapping("/bill/{billId}/refunds")
    @Operation(summary = "Fatura iadelerini getir", description = "Belirtilen faturaya ait tüm iadeleri getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "İadeler başarıyla getirildi"),
            @ApiResponse(responseCode = "403", description = "Yetkilendirme hatası"),
            @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @billQueryService.isBillBelongsToCustomer(#billId, authentication.principal.username))")
    public ResponseEntity<List<RefundResponse>> getRefundsByBill(@PathVariable UUID billId) {
        if (!defaultBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        log.info("Fetching refunds for bill ID: {}", billId);
        return ResponseEntity.ok(commandService.getRefundsByBill(billId));
    }

    /**
     * Belirtilen ID'ye sahip iadeyi getirir.
     *
     * @param refundId İade ID'si
     * @return İade detayları
     */
    @GetMapping("/refunds/{refundId}")
    @Operation(summary = "İade detayı getir", description = "Belirtilen ID'ye sahip iadenin detaylarını getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "İade başarıyla getirildi"),
            @ApiResponse(responseCode = "404", description = "İade bulunamadı"),
            @ApiResponse(responseCode = "403", description = "Yetkilendirme hatası"),
            @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @paymentQueryService.isRefundBelongsToCustomer(#refundId, authentication.principal.username))")
    public ResponseEntity<RefundResponse> getRefund(@PathVariable UUID refundId) {
        if (!defaultBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        log.info("Fetching refund with ID: {}", refundId);
        RefundResponse response = queryService.getRefund(refundId);
        return ResponseEntity.ok(response);
    }

    /**
     * Belirtilen iadeyi siler.
     *
     * @param refundId Silinecek iade ID'si
     * @return İşlem sonucu
     */
    @DeleteMapping("/refunds/{refundId}")
    @Operation(summary = "İade sil", description = "Belirtilen ID'ye sahip iadeyi siler")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "İade başarıyla silindi"),
            @ApiResponse(responseCode = "404", description = "İade bulunamadı"),
            @ApiResponse(responseCode = "403", description = "Yetkilendirme hatası"),
            @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @paymentQueryService.isRefundBelongsToCustomer(#refundId, authentication.principal.username))")
    public ResponseEntity<Void> deleteRefund(@PathVariable UUID refundId) {
        if (!defaultBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        log.info("Deleting refund with ID: {}", refundId);
        commandService.deleteRefund(refundId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/methods")
    @Operation(summary = "Kullanılabilir ödeme yöntemlerini getir", description = "Sistemde kullanılabilir ödeme yöntemlerini listeler")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ödeme yöntemleri başarıyla getirildi"),
            @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    })
    public ResponseEntity<List<String>> getPaymentMethods() {
        if (!defaultBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        log.info("Fetching available payment methods");
        List<String> methods = queryService.getPaymentMethods();
        return ResponseEntity.ok(methods);
    }
}