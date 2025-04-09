package com.turkcell.billingservice.controllers;

import com.turkcell.billingservice.dtos.responses.BillingReportResponse;
import com.turkcell.billingservice.services.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Billing report endpoints")
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/billing")
    @Operation(summary = "Generate billing report for a customer")
    public ResponseEntity<BillingReportResponse> generateBillingReport(
            @RequestParam Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        BillingReportResponse response = reportService.generateBillingReport(customerId, startDate, endDate);
        return ResponseEntity.ok(response);
    }
} 