package com.turkcell.billingservice.controllers;

import com.turkcell.billingservice.clients.ContractServiceClient;
import com.turkcell.billingservice.clients.CustomerServiceClient;
import com.turkcell.billingservice.clients.PlanServiceClient;
import com.turkcell.billingservice.domain.dtos.requests.BillRequest;
import com.turkcell.billingservice.domain.dtos.responses.BillResponse;
import com.turkcell.billingservice.domain.dtos.responses.ContractResponse;
import com.turkcell.billingservice.domain.dtos.responses.CustomerResponse;
import com.turkcell.billingservice.domain.dtos.responses.PlanResponse;
import com.turkcell.billingservice.domain.enums.BillStatus;
import com.turkcell.billingservice.services.BillCommandService;
import com.turkcell.billingservice.services.BillQueryService;
import com.turkcell.billingservice.services.PdfService;
import io.github.bucket4j.Bucket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/bills")
@Tag(name = "Bill API", description = "Faturalandırma işlemleri")
@RequiredArgsConstructor
@Slf4j
public class BillController {
    private final BillCommandService commandService;
    private final BillQueryService queryService;
    private final PdfService pdfService;
    private final CustomerServiceClient customerServiceClient;
    private final ContractServiceClient contractServiceClient;
    private final PlanServiceClient planServiceClient;
    @Qualifier("billApiBucket")
    private final Bucket rateLimitBucket;

    @PostMapping
    @Operation(
        summary = "Yeni fatura oluşturur",
        description = "Müşteri ve sözleşme bilgilerine göre yeni bir fatura oluşturur",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Fatura başarıyla oluşturuldu"),
        @ApiResponse(responseCode = "400", description = "Geçersiz istek verisi"),
        @ApiResponse(responseCode = "401", description = "Yetkilendirme hatası"),
        @ApiResponse(responseCode = "429", description = "Çok fazla istek gönderildi")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillResponse> createBill(@Valid @RequestBody BillRequest request) {
        if (rateLimitBucket.tryConsume(1)) {
            log.info("Creating new bill for customer: {}", request.getCustomerId());
            BillResponse response = commandService.createBill(request).join();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    @PostMapping("/batch")
    @Operation(
        summary = "Faturaları toplu oluşturur",
        description = "Birden fazla faturayı tek bir istek ile oluşturur",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Faturalar başarıyla oluşturuldu"),
        @ApiResponse(responseCode = "400", description = "Geçersiz istek verileri"),
        @ApiResponse(responseCode = "401", description = "Yetkilendirme hatası"),
        @ApiResponse(responseCode = "429", description = "Çok fazla istek gönderildi")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BillResponse>> createBillsBatch(@Valid @RequestBody List<BillRequest> requests) {
        if (rateLimitBucket.tryConsume(1)) {
            log.info("Creating batch bills, count: {}", requests.size());
            List<BillResponse> responses = commandService.createBillsBatch(requests);
            return ResponseEntity.status(HttpStatus.CREATED).body(responses);
        }
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    @GetMapping
    @Operation(summary = "Tüm faturaları getir", description = "Sistemdeki tüm faturaları sayfalı olarak listeler")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Faturalar başarıyla getirildi"),
            @ApiResponse(responseCode = "403", description = "Yetkilendirme hatası"),
            @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<BillResponse>> getAllBills(Pageable pageable) {
        if (!rateLimitBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        log.info("Fetching all bills with pagination");
        Page<BillResponse> bills = queryService.getAllBills(pageable);
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/{billId}")
    @Operation(
        summary = "Fatura detaylarını getirir",
        description = "Belirtilen ID'ye sahip faturanın detaylarını getirir",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Fatura başarıyla getirildi"),
        @ApiResponse(responseCode = "404", description = "Fatura bulunamadı"),
        @ApiResponse(responseCode = "401", description = "Yetkilendirme hatası"),
        @ApiResponse(responseCode = "429", description = "Çok fazla istek gönderildi")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @billQueryService.isBillBelongsToCustomer(#billId, authentication.principal.username))")
    public ResponseEntity<BillResponse> getBill(
            @Parameter(description = "Fatura ID", required = true) 
            @PathVariable UUID billId) {
        if (rateLimitBucket.tryConsume(1)) {
            log.info("Fetching bill with ID: {}", billId);
            BillResponse response = queryService.getBill(billId);
            
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    @GetMapping("/customer/{customerId}")
    @Operation(
        summary = "Müşteriye ait faturaları getirir",
        description = "Belirtilen müşteriye ait faturaları sayfalı olarak getirir",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Faturalar başarıyla getirildi"),
        @ApiResponse(responseCode = "401", description = "Yetkilendirme hatası"),
        @ApiResponse(responseCode = "429", description = "Çok fazla istek gönderildi")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #customerId.toString() == authentication.principal.username)")
    public ResponseEntity<Page<BillResponse>> getBillsByCustomer(
            @Parameter(description = "Müşteri ID", required = true) 
            @PathVariable UUID customerId, 
            Pageable pageable) {
        if (rateLimitBucket.tryConsume(1)) {
            log.info("Fetching bills for customer ID: {}", customerId);
            Page<BillResponse> bills = queryService.getBillsByCustomer(customerId, pageable);
            return ResponseEntity.ok(bills);
        }
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    @GetMapping("/status/{status}")
    @Operation(
        summary = "Duruma göre faturaları getirir",
        description = "Belirtilen duruma sahip faturaları sayfalı olarak getirir",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Faturalar başarıyla getirildi"),
        @ApiResponse(responseCode = "400", description = "Geçersiz durum"),
        @ApiResponse(responseCode = "401", description = "Yetkilendirme hatası"),
        @ApiResponse(responseCode = "429", description = "Çok fazla istek gönderildi")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<BillResponse>> getBillsByStatus(
            @Parameter(description = "Fatura durumu (PENDING, PAID, OVERDUE, CANCELLED)", required = true) 
            @PathVariable String status, 
            Pageable pageable) {
        if (rateLimitBucket.tryConsume(1)) {
            log.info("Fetching bills with status: {}", status);
            Page<BillResponse> bills = queryService.getBillsByStatus(status, pageable);
            return ResponseEntity.ok(bills);
        }
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    @GetMapping("/date-range")
    @Operation(summary = "Tarih aralığına göre faturaları getir", description = "Belirtilen tarih aralığında faturaları getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Faturalar başarıyla getirildi"),
            @ApiResponse(responseCode = "400", description = "Geçersiz tarih formatı"),
            @ApiResponse(responseCode = "403", description = "Yetkilendirme hatası"),
            @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #customerId.toString() == authentication.principal.username)")
    public ResponseEntity<Page<BillResponse>> getBillsByDateRange(
            @RequestParam UUID customerId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            Pageable pageable) {
        if (!rateLimitBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        log.info("Fetching bills for customer ID: {} between {} and {}", customerId, startDate, endDate);
        Page<BillResponse> bills = queryService.getBillsByCustomerAndDateRange(customerId, startDate, endDate, pageable);
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/unpaid")
    @Operation(summary = "Ödenmemiş faturaları getir", description = "Ödenmemiş faturaları listeler")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ödenmemiş faturalar başarıyla getirildi"),
            @ApiResponse(responseCode = "403", description = "Yetkilendirme hatası"),
            @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #customerId.toString() == authentication.principal.username)")
    public ResponseEntity<Page<BillResponse>> getUnpaidBills(@RequestParam UUID customerId, Pageable pageable) {
        if (!rateLimitBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        log.info("Fetching unpaid bills for customer ID: {}", customerId);
        Page<BillResponse> bills = queryService.getUnpaidBills(customerId, pageable);
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/overdue")
    @Operation(summary = "Gecikmiş faturaları getir", description = "Gecikmiş faturaları listeler")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gecikmiş faturalar başarıyla getirildi"),
            @ApiResponse(responseCode = "403", description = "Yetkilendirme hatası"),
            @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #customerId.toString() == authentication.principal.username)")
    public ResponseEntity<Page<BillResponse>> getOverdueBills(@RequestParam UUID customerId, Pageable pageable) {
        if (!rateLimitBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        log.info("Fetching overdue bills for customer ID: {}", customerId);
        Page<BillResponse> bills = queryService.getOverdueBills(customerId, pageable);
        return ResponseEntity.ok(bills);
    }

    @PutMapping("/{billId}/status")
    @Operation(summary = "Fatura durumunu güncelle", description = "Belirtilen faturanın durumunu günceller")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fatura durumu başarıyla güncellendi"),
            @ApiResponse(responseCode = "400", description = "Geçersiz durum"),
            @ApiResponse(responseCode = "404", description = "Fatura bulunamadı"),
            @ApiResponse(responseCode = "403", description = "Yetkilendirme hatası"),
            @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateBillStatus(
            @PathVariable UUID billId,
            @RequestParam @Parameter(description = "Yeni durum: PENDING, PAID, OVERDUE, CANCELLED, REFUNDED") String status) {
        if (!rateLimitBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        log.info("Updating status for billId: {} to {}", billId, status);
        commandService.updateBillStatus(billId, status);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{billId}/cancel")
    @Operation(summary = "Faturayı iptal et", description = "Belirtilen faturayı iptal eder")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fatura başarıyla iptal edildi"),
            @ApiResponse(responseCode = "400", description = "Fatura iptal edilemez"),
            @ApiResponse(responseCode = "404", description = "Fatura bulunamadı"),
            @ApiResponse(responseCode = "403", description = "Yetkilendirme hatası"),
            @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillResponse> cancelBill(@PathVariable UUID billId) {
        if (!rateLimitBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        log.info("Cancelling bill with ID: {}", billId);
        BillResponse response = commandService.cancelBill(billId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{billId}")
    @Operation(summary = "Fatura kaydını sil", description = "Belirtilen fatura kaydını siler")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Fatura başarıyla silindi"),
            @ApiResponse(responseCode = "404", description = "Fatura bulunamadı"),
            @ApiResponse(responseCode = "403", description = "Yetkilendirme hatası"),
            @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBill(@PathVariable UUID billId) {
        if (!rateLimitBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        log.info("Deleting bill with ID: {}", billId);
        commandService.deleteBill(billId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{billId}/pdf")
    @Operation(summary = "Fatura PDF indir", description = "Belirtilen faturayı PDF formatında indirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF başarıyla indirildi"),
            @ApiResponse(responseCode = "404", description = "Fatura bulunamadı"),
            @ApiResponse(responseCode = "403", description = "Yetkilendirme hatası"),
            @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @billQueryService.isBillBelongsToCustomer(#billId, authentication.principal.username))")
    public ResponseEntity<byte[]> downloadBillPdf(@PathVariable UUID billId) {
        if (!rateLimitBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        log.info("Generating PDF for bill ID: {}", billId);
        BillResponse bill = queryService.getBill(billId);
        CustomerResponse customer = customerServiceClient.getCustomer(bill.getCustomerId());
        ContractResponse contract = contractServiceClient.getContract(bill.getContractId());
        PlanResponse plan = planServiceClient.getPlan(Long.parseLong(contract.getPlanId()));

        byte[] pdfBytes = pdfService.generateBillPdf(bill, customer, plan);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "bill-" + billId + ".pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}