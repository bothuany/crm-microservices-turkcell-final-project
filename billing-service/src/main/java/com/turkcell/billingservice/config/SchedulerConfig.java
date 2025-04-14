package com.turkcell.billingservice.config;

import com.turkcell.billingservice.services.BillCommandService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    private final BillCommandService billCommandService;

    public SchedulerConfig(BillCommandService billCommandService) {
        this.billCommandService = billCommandService;
    }

    /**
     * Her gün saat 09:00'da fatura hatırlatmalarını kontrol eder
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkBillReminders() {
        billCommandService.sendBillReminders();
    }
} 