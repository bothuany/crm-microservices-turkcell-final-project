package com.turkcell.billingservice.controllers;

import com.turkcell.billingservice.domain.dtos.responses.BillingReportResponse;
import com.turkcell.billingservice.services.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Report API", description = "Raporlama işlemleri")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    
    @GetMapping("/billing")
    @Operation(summary = "Faturalandırma raporu oluşturur", description = "Belirli tarih aralığında ve müşteri için faturalandırma raporu oluşturur")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #customerId == authentication.principal.customerId)")
    public ResponseEntity<BillingReportResponse> generateBillingReport(
            @RequestParam UUID customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateBillingReport(customerId, startDate, endDate));
    }
} 