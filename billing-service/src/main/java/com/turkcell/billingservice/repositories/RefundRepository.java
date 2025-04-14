package com.turkcell.billingservice.repositories;

import com.turkcell.billingservice.domain.entities.Refund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {
    List<Refund> findByPaymentId(UUID paymentId);
    List<Refund> findByBillId(UUID billId);
    List<Refund> findByCustomerId(UUID customerId);
    Page<Refund> findByCustomerId(UUID customerId, Pageable pageable);
    List<Refund> findByPaymentIdIn(List<UUID> paymentIds);
    boolean existsByIdAndCustomerId(UUID id, UUID customerId);
} 