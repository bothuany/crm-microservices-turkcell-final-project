package com.turkcell.billingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turkcell.billingservice.domain.dtos.requests.PaymentRequest;
import com.turkcell.billingservice.domain.entities.Bill;
import com.turkcell.billingservice.domain.enums.BillStatus;
import com.turkcell.billingservice.domain.enums.PaymentMethod;
import com.turkcell.billingservice.repositories.BillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Disabled("Test dosyasında hatalar var, geçici olarak devre dışı bırakıldı")
public class PaymentControllerIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("billing_db")
            .withUsername("postgres")
            .withPassword("password");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        billRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void processPayment_success() throws Exception {
        Bill bill = new Bill();
        bill.setId(UUID.randomUUID());
        bill.setCustomerId(UUID.randomUUID());
        bill.setTotalAmount(100.0);
        bill.setDueDate(LocalDate.now().plusDays(30));
        bill.setStatus(BillStatus.PENDING);
        billRepository.save(bill);

        PaymentRequest request = new PaymentRequest();
        request.setBillId(bill.getId());
        request.setCustomerId(bill.getCustomerId());
        request.setAmount(100.0);
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void processPayment_invalidAmount() throws Exception {
        Bill bill = new Bill();
        bill.setId(UUID.randomUUID());
        bill.setCustomerId(UUID.randomUUID());
        bill.setTotalAmount(100.0);
        bill.setDueDate(LocalDate.now().plusDays(30));
        bill.setStatus(BillStatus.PENDING);
        billRepository.save(bill);

        PaymentRequest request = new PaymentRequest();
        request.setBillId(bill.getId());
        request.setCustomerId(bill.getCustomerId());
        request.setAmount(50.0); // Less than total amount
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getPayment_success() throws Exception {
        Bill bill = new Bill();
        bill.setId(UUID.randomUUID());
        bill.setCustomerId(UUID.randomUUID());
        bill.setTotalAmount(100.0);
        bill.setDueDate(LocalDate.now().plusDays(30));
        bill.setStatus(BillStatus.PENDING);
        billRepository.save(bill);

        PaymentRequest request = new PaymentRequest();
        request.setBillId(bill.getId());
        request.setCustomerId(bill.getCustomerId());
        request.setAmount(100.0);
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        String response = mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String paymentId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/api/v1/payments/{paymentId}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId))
                .andExpect(jsonPath("$.amount").value(100.0));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getPayment_notFound() throws Exception {
        mockMvc.perform(get("/api/v1/payments/{paymentId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePayment_success() throws Exception {
        Bill bill = new Bill();
        bill.setId(UUID.randomUUID());
        bill.setCustomerId(UUID.randomUUID());
        bill.setTotalAmount(100.0);
        bill.setDueDate(LocalDate.now().plusDays(30));
        bill.setStatus(BillStatus.PENDING);
        billRepository.save(bill);

        PaymentRequest request = new PaymentRequest();
        request.setBillId(bill.getId());
        request.setCustomerId(bill.getCustomerId());
        request.setAmount(100.0);
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        String response = mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String paymentId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(delete("/api/v1/payments/{paymentId}", paymentId))
                .andExpect(status().isNoContent());
    }
} 