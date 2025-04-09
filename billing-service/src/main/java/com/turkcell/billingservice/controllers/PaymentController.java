package com.turkcell.billingservice.controllers;

import com.turkcell.billingservice.dtos.requests.CreatePaymentRequest;
import com.turkcell.billingservice.dtos.responses.PaymentResponse;
import com.turkcell.billingservice.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management endpoints")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Create a new payment")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        PaymentResponse response = paymentService.getPayment(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get payments by customer ID")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByCustomer(@PathVariable Long customerId) {
        List<PaymentResponse> response = paymentService.getPaymentsByCustomer(customerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bill/{billId}")
    @Operation(summary = "Get payments by bill ID")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByBill(@PathVariable Long billId) {
        List<PaymentResponse> response = paymentService.getPaymentsByBill(billId);
        return ResponseEntity.ok(response);
    }
} 