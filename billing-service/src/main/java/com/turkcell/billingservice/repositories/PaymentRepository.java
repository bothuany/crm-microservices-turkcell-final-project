package com.turkcell.billingservice.repositories;

import com.turkcell.billingservice.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByCustomerId(Long customerId);
    List<Payment> findByBillId(Long billId);
    List<Payment> findByCustomerIdAndCreatedAtTimestampBetween(Long customerId, LocalDateTime startDate, LocalDateTime endDate);
}