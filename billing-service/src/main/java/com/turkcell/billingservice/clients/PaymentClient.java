package com.turkcell.billingservice.clients;

import com.turkcell.billingservice.dtos.requests.CreatePaymentRequest;
import com.turkcell.billingservice.dtos.responses.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentClient {
    @PostMapping("/api/v1/payments")
    PaymentResponse processPayment(@RequestBody CreatePaymentRequest request);
} 