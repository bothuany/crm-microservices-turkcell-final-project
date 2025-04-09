package com.turkcell.billingservice.services;

import com.turkcell.billingservice.entities.Bill;
import com.turkcell.billingservice.repositories.BillRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillReminderService {
    private static final Logger logger = LoggerFactory.getLogger(BillReminderService.class);
    
    private final BillRepository billRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Her gün sabah 9'da çalışır
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkOverdueBills() {
        logger.info("Checking for overdue bills...");
        List<Bill> overdueBills = billRepository.findByDueDateLessThanAndStatus(LocalDateTime.now(), "PENDING");
        
        for (Bill bill : overdueBills) {
            // Fatura durumunu güncelle
            bill.setStatus("OVERDUE");
            billRepository.save(bill);
            
            // Kafka event gönder
            kafkaTemplate.send("bill-overdue", bill);
            
            logger.info("Bill {} marked as overdue and notification sent", bill.getId());
        }
        
        logger.info("Overdue bill check completed. {} bills marked as overdue", overdueBills.size());
    }

    // Her ayın 1'i ve 15'inde saat 10'da çalışır
    @Scheduled(cron = "0 0 10 1,15 * ?")
    public void sendPaymentReminders() {
        logger.info("Sending payment reminders for pending bills...");
        List<Bill> pendingBills = billRepository.findByStatus("PENDING");
        
        for (Bill bill : pendingBills) {
            // Kafka event gönder
            kafkaTemplate.send("payment-reminder", bill);
            
            logger.info("Payment reminder sent for bill {}", bill.getId());
        }
        
        logger.info("Payment reminders sent for {} bills", pendingBills.size());
    }
} 