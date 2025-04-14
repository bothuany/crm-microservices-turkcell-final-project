package com.turkcell.billingservice.repositories;

import com.turkcell.billingservice.domain.entities.Bill;
import com.turkcell.billingservice.domain.enums.BillStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BillRepository extends JpaRepository<Bill, UUID> {
    Page<Bill> findByCustomerId(UUID customerId, Pageable pageable);
    Page<Bill> findByStatus(BillStatus status, Pageable pageable);
    Page<Bill> findByCustomerIdAndStatus(UUID customerId, BillStatus status, Pageable pageable);
    Page<Bill> findByCustomerIdAndDueDateBetween(UUID customerId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<Bill> findByCustomerId(UUID customerId);
    List<Bill> findByStatus(BillStatus status);
    List<Bill> findByCustomerIdAndDueDateBetween(UUID customerId, LocalDate startDate, LocalDate endDate);
    
    // Vadesi yaklaşan faturaları bulmak için
    List<Bill> findByDueDateBetweenAndStatus(LocalDate startDate, LocalDate endDate, BillStatus status);
    
    // Vadesi geçmiş faturaları bulmak için
    List<Bill> findByDueDateBeforeAndStatus(LocalDate date, BillStatus status);
} 