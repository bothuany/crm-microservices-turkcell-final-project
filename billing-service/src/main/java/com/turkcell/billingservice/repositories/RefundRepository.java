package com.turkcell.billingservice.repositories;

import com.turkcell.billingservice.entities.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findByCustomerId(Long customerId);
    List<Refund> findByBillId(Long billId);
    List<Refund> findByPaymentId(Long paymentId);
    List<Refund> findByCustomerIdAndCreatedAtBetween(Long customerId, LocalDateTime startDate, LocalDateTime endDate);
} 