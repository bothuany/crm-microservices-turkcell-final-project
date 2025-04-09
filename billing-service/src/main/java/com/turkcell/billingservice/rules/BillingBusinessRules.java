package com.turkcell.billingservice.rules;

import com.turkcell.billingservice.entities.Bill;
import com.turkcell.billingservice.exceptions.BusinessException;
import com.turkcell.billingservice.repositories.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BillingBusinessRules {
    private final BillRepository billRepository;

    public void checkIfBillExists(Long id) {
        if (!billRepository.existsById(id)) {
            throw new BusinessException("Fatura bulunamadı: " + id);
        }
    }

    public void checkIfBillAlreadyPaid(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Fatura bulunamadı"));
        if (bill.getStatus().equals("PAID")) {
            throw new BusinessException("Fatura zaten ödenmiş");
        }
    }

    public void validatePaymentAmount(Long billId, double paymentAmount) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BusinessException("Fatura bulunamadı"));
        if (paymentAmount != bill.getAmount()) {
            throw new BusinessException("Ödeme tutarı fatura tutarı ile eşleşmiyor");
        }
    }

    public void validateDueDate(LocalDate dueDate) {
        if (dueDate.isBefore(LocalDate.now())) {
            throw new BusinessException("Son ödeme tarihi geçmiş tarih olamaz");
        }
    }

    public void validateAmount(double amount) {
        if (amount <= 0) {
            throw new BusinessException("Fatura tutarı sıfır veya negatif olamaz");
        }
    }
}