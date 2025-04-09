package com.turkcell.billingservice.controllers;

import com.turkcell.billingservice.dtos.requests.CreateBillRequest;
import com.turkcell.billingservice.dtos.responses.BillResponse;
import com.turkcell.billingservice.services.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/bills")
@RequiredArgsConstructor
@Tag(name = "Billing Controller", description = "Billing management APIs")
public class BillingController {
    private final BillingService billingService;

    @PostMapping
    @Operation(summary = "Create a new bill")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Bill created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Customer or Contract not found")
    })
    public ResponseEntity<BillResponse> createBill(@Valid @RequestBody CreateBillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(billingService.createBill(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bill by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bill found"),
        @ApiResponse(responseCode = "404", description = "Bill not found")
    })
    public ResponseEntity<BillResponse> getBill(
            @Parameter(description = "Bill ID") @PathVariable Long id) {
        return ResponseEntity.ok(billingService.getBill(id));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all bills by customer ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bills found"),
            @ApiResponse(responseCode = "404", description = "No bills found for customer")
    })
    public ResponseEntity<List<BillResponse>> getBillsByCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {
        return ResponseEntity.ok(billingService.getBillsByCustomer(customerId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get all bills by status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bills found"),
            @ApiResponse(responseCode = "404", description = "No bills found with status")
    })
    public ResponseEntity<List<BillResponse>> getBillsByStatus(
            @Parameter(description = "Bill status") @PathVariable String status) {
        return ResponseEntity.ok(billingService.getBillsByStatus(status));
    }

    @GetMapping("/customer/{customerId}/date-range")
    @Operation(summary = "Get bills by date range for a customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bills found"),
            @ApiResponse(responseCode = "404", description = "No bills found in date range")
    })
    public ResponseEntity<List<BillResponse>> getBillsByDateRange(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(billingService.getBillsByDateRange(customerId, startDate, endDate));
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get all overdue bills")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Overdue bills found"),
            @ApiResponse(responseCode = "404", description = "No overdue bills found")
    })
    public ResponseEntity<List<BillResponse>> getOverdueBills() {
        return ResponseEntity.ok(billingService.getOverdueBills());
    }

    @PutMapping("/{id}/pay")
    @Operation(summary = "Mark bill as paid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bill marked as paid"),
            @ApiResponse(responseCode = "404", description = "Bill not found"),
            @ApiResponse(responseCode = "400", description = "Bill is not in PENDING status")
    })
    public ResponseEntity<BillResponse> payBill(
            @Parameter(description = "Bill ID") @PathVariable Long id) {
        return ResponseEntity.ok(billingService.payBill(id));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel bill")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bill cancelled"),
            @ApiResponse(responseCode = "404", description = "Bill not found"),
            @ApiResponse(responseCode = "400", description = "Bill is not in PENDING status")
    })
    public ResponseEntity<BillResponse> cancelBill(
            @Parameter(description = "Bill ID") @PathVariable Long id) {
        return ResponseEntity.ok(billingService.cancelBill(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update bill")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bill updated"),
            @ApiResponse(responseCode = "404", description = "Bill not found"),
            @ApiResponse(responseCode = "400", description = "Bill is not in PENDING status")
    })
    public ResponseEntity<BillResponse> updateBill(
            @Parameter(description = "Bill ID") @PathVariable Long id,
            @Valid @RequestBody CreateBillRequest request) {
        return ResponseEntity.ok(billingService.updateBill(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete bill")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bill deleted"),
            @ApiResponse(responseCode = "404", description = "Bill not found"),
            @ApiResponse(responseCode = "400", description = "Bill is not in PENDING status")
    })
    public ResponseEntity<Void> deleteBill(
            @Parameter(description = "Bill ID") @PathVariable Long id) {
        billingService.deleteBill(id);
        return ResponseEntity.noContent().build();
    }
}