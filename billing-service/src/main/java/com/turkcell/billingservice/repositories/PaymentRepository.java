package com.turkcell.billingservice.repositories;

import com.turkcell.billingservice.domain.entities.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Page<Payment> findByCustomerId(UUID customerId, Pageable pageable);
    Page<Payment> findByBillId(UUID billId, Pageable pageable);
    List<Payment> findByCustomerIdAndCreatedAtBetween(UUID customerId, LocalDateTime startDate, LocalDateTime endDate);
    List<Payment> findByBillIdIn(List<UUID> billIds);
    boolean existsByIdAndCustomerId(UUID id, UUID customerId);
}