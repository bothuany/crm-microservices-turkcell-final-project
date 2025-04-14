package com.turkcell.billingservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turkcell.billingservice.domain.dtos.requests.BillRequest;
import com.turkcell.billingservice.domain.entities.Bill;
import com.turkcell.billingservice.domain.enums.BillStatus;
import com.turkcell.billingservice.repositories.BillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
class BillControllerIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("billing_test")
            .withUsername("postgres")
            .withPassword("password");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID customerId;
    private UUID contractId;
    private BillRequest billRequest;

    @BeforeEach
    void setUp() {
        // Önceki testlerden kalan verileri temizle
        billRepository.deleteAll();
        
        // Test verileri oluştur
        customerId = UUID.randomUUID();
        contractId = UUID.randomUUID();
        
        billRequest = new BillRequest();
        billRequest.setCustomerId(customerId);
        billRequest.setContractId(contractId);
        billRequest.setTotalAmount(100.0);
        billRequest.setDueDate(LocalDate.now().plusDays(30));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createBill_whenValid_thenReturn201() throws Exception {
        mockMvc.perform(post("/api/v1/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(billRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId", is(customerId.toString())))
                .andExpect(jsonPath("$.contractId", is(contractId.toString())))
                .andExpect(jsonPath("$.totalAmount", is(100.0)))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBill_whenExists_thenReturn200() throws Exception {
        // Önce bir fatura oluşturalım
        Bill bill = Bill.builder()
                .customerId(customerId)
                .contractId(contractId)
                .totalAmount(100.0)
                .status(BillStatus.PENDING)
                .dueDate(LocalDate.now().plusDays(30))
                .build();
        
        Bill savedBill = billRepository.save(bill);
        
        mockMvc.perform(get("/api/v1/bills/{billId}", savedBill.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedBill.getId().toString())))
                .andExpect(jsonPath("$.customerId", is(customerId.toString())))
                .andExpect(jsonPath("$.contractId", is(contractId.toString())))
                .andExpect(jsonPath("$.totalAmount", is(100.0)))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER", username = "other-user")
    void getBill_whenNotOwner_thenReturn403() throws Exception {
        // Önce bir fatura oluşturalım
        Bill bill = Bill.builder()
                .customerId(customerId) // username'den farklı olduğuna dikkat edin
                .contractId(contractId)
                .totalAmount(100.0)
                .status(BillStatus.PENDING)
                .dueDate(LocalDate.now().plusDays(30))
                .build();
        
        Bill savedBill = billRepository.save(bill);
        
        mockMvc.perform(get("/api/v1/bills/{billId}", savedBill.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBill_whenUnauthorized_thenReturn401() throws Exception {
        // Önce bir fatura oluşturalım
        Bill bill = Bill.builder()
                .customerId(customerId)
                .contractId(contractId)
                .totalAmount(100.0)
                .status(BillStatus.PENDING)
                .dueDate(LocalDate.now().plusDays(30))
                .build();
        
        Bill savedBill = billRepository.save(bill);
        
        mockMvc.perform(get("/api/v1/bills/{billId}", savedBill.getId()))
                .andExpect(status().isUnauthorized());
    }
} 