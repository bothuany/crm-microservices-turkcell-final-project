package com.turkcell.billingservice.repositories;

import com.turkcell.billingservice.entities.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByCustomerId(Long customerId);
    List<Bill> findByCustomerIdAndStatus(Long customerId, String status);
    List<Bill> findByStatus(String status);
    List<Bill> findByCustomerIdAndCreatedAtBetween(Long customerId, LocalDateTime startDate, LocalDateTime endDate);
    List<Bill> findByDueDateLessThanAndStatus(LocalDateTime date, String status);
} 