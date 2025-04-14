package com.turkcell.billingservice.validation;

import com.turkcell.billingservice.domain.entities.Bill;
import com.turkcell.billingservice.domain.enums.BillStatus;
import com.turkcell.billingservice.exceptions.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class BillingBusinessRules {
    public void checkIfBillExists(Bill bill) {
        if (bill == null) {
            throw new BusinessException("Bill not found");
        }
    }

    public void checkIfBillIsPaid(Bill bill) {
        if (bill.getStatus() == BillStatus.PAID) {
            throw new BusinessException("Bill is already paid");
        }
    }

    public void checkIfBillIsCancelled(Bill bill) {
        if (bill.getStatus() == BillStatus.CANCELLED) {
            throw new BusinessException("Bill is cancelled");
        }
    }

    public void checkIfBillIsOverdue(Bill bill) {
        if (bill.getStatus() == BillStatus.OVERDUE) {
            throw new BusinessException("Bill is overdue");
        }
    }

    public void checkIfAmountIsValid(double amount) {
        if (amount <= 0) {
            throw new BusinessException("Amount must be greater than 0");
        }
    }
} 