package com.turkcell.billingservice.controllers;

import com.turkcell.billingservice.dtos.requests.CreateRefundRequest;
import com.turkcell.billingservice.dtos.responses.RefundResponse;
import com.turkcell.billingservice.services.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/refunds")
@RequiredArgsConstructor
@Tag(name = "Refunds", description = "Refund management endpoints")
public class RefundController {
    private final RefundService refundService;

    @PostMapping
    @Operation(summary = "Create a new refund")
    public ResponseEntity<RefundResponse> createRefund(@Valid @RequestBody CreateRefundRequest request) {
        RefundResponse response = refundService.createRefund(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get refund by ID")
    public ResponseEntity<RefundResponse> getRefund(@PathVariable Long id) {
        RefundResponse response = refundService.getRefund(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get refunds by customer ID")
    public ResponseEntity<List<RefundResponse>> getRefundsByCustomer(@PathVariable Long customerId) {
        List<RefundResponse> response = refundService.getRefundsByCustomer(customerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bill/{billId}")
    @Operation(summary = "Get refunds by bill ID")
    public ResponseEntity<List<RefundResponse>> getRefundsByBill(@PathVariable Long billId) {
        List<RefundResponse> response = refundService.getRefundsByBill(billId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payment/{paymentId}")
    @Operation(summary = "Get refunds by payment ID")
    public ResponseEntity<List<RefundResponse>> getRefundsByPayment(@PathVariable Long paymentId) {
        List<RefundResponse> response = refundService.getRefundsByPayment(paymentId);
        return ResponseEntity.ok(response);
    }
} 