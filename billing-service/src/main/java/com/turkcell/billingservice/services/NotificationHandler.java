package com.turkcell.billingservice.services;

import com.turkcell.billingservice.clients.NotificationServiceClient;
import com.turkcell.billingservice.domain.dtos.requests.NotificationRequest;
import com.turkcell.billingservice.domain.dtos.responses.CustomerResponse;
import com.turkcell.billingservice.domain.entities.Bill;
import com.turkcell.billingservice.domain.entities.Payment;
import com.turkcell.billingservice.domain.entities.Refund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Bildirim gönderme işlemlerini yönetir.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationHandler {
    private final NotificationServiceClient notificationServiceClient;

    /**
     * Fatura oluşturuldu bildirimini gönderir.
     *
     * @param bill Fatura entity
     * @param customer Müşteri bilgileri
     */
    @Async("taskExecutor")
    public void sendBillCreatedNotification(Bill bill, CustomerResponse customer) {
        log.info("Sending bill created notification for billId: {}", bill.getId());
        NotificationRequest request = NotificationRequest.builder()
                .recipient(customer.getEmail())
                .subject("Yeni Fatura Oluşturuldu")
                .body("Sayın " + customer.getName() + ",\n\n" +
                        "Yeni bir fatura oluşturulmuştur.\n" +
                        "Fatura ID: " + bill.getId() + "\n" +
                        "Toplam Tutar: " + bill.getTotalAmount() + "\n" +
                        "Vade Tarihi: " + bill.getDueDate() + "\n\n" +
                        "İyi günler dileriz.")
                .build();
        notificationServiceClient.sendNotification(request);
    }

    /**
     * Ödeme onayı bildirimini gönderir.
     *
     * @param payment Ödeme entity
     * @param customer Müşteri bilgileri
     */
    @Async("taskExecutor")
    public void sendPaymentConfirmation(Payment payment, CustomerResponse customer) {
        log.info("Sending payment confirmation for paymentId: {}", payment.getId());
        NotificationRequest request = NotificationRequest.builder()
                .recipient(customer.getEmail())
                .subject("Ödeme Onayı")
                .body("Sayın " + customer.getName() + ",\n\n" +
                        "Ödemeniz başarıyla alınmıştır.\n" +
                        "Ödeme ID: " + payment.getId() + "\n" +
                        "Fatura ID: " + payment.getBillId() + "\n" +
                        "Tutar: " + payment.getAmount() + "\n" +
                        "Ödeme Yöntemi: " + payment.getPaymentMethod() + "\n\n" +
                        "İyi günler dileriz.")
                .build();
        notificationServiceClient.sendNotification(request);
    }
    
    /**
     * İade onayı bildirimini gönderir.
     *
     * @param refund İade entity
     * @param customer Müşteri bilgileri
     */
    @Async("taskExecutor")
    public void sendRefundConfirmation(Refund refund, CustomerResponse customer) {
        log.info("Sending refund confirmation for refundId: {}", refund.getId());
        NotificationRequest request = NotificationRequest.builder()
                .recipient(customer.getEmail())
                .subject("İade Onayı")
                .body("Sayın " + customer.getName() + ",\n\n" +
                        "İade talebiniz başarıyla işleme alınmıştır.\n" +
                        "İade ID: " + refund.getId() + "\n" +
                        "Ödeme ID: " + refund.getPaymentId() + "\n" +
                        "Fatura ID: " + refund.getBillId() + "\n" +
                        "Tutar: " + refund.getAmount() + "\n" +
                        "Neden: " + refund.getReason() + "\n\n" +
                        "İyi günler dileriz.")
                .build();
        notificationServiceClient.sendNotification(request);
    }

    /**
     * Yaklaşan fatura hatırlatma bildirimini gönderir.
     *
     * @param bill Fatura entity
     * @param customer Müşteri bilgileri
     */
    @Async("taskExecutor")
    public void sendUpcomingBillReminder(Bill bill, CustomerResponse customer) {
        log.info("Sending upcoming bill reminder for billId: {}", bill.getId());
        NotificationRequest request = NotificationRequest.builder()
                .recipient(customer.getEmail())
                .subject("Fatura Ödeme Hatırlatması")
                .body("Sayın " + customer.getName() + ",\n\n" +
                        "Bir faturanızın vade tarihi yaklaşmaktadır.\n" +
                        "Fatura ID: " + bill.getId() + "\n" +
                        "Toplam Tutar: " + bill.getTotalAmount() + "\n" +
                        "Vade Tarihi: " + bill.getDueDate() + "\n\n" +
                        "Lütfen ödemenizi vade tarihinden önce gerçekleştiriniz.\n" +
                        "İyi günler dileriz.")
                .build();
        notificationServiceClient.sendNotification(request);
    }
    
    /**
     * Gecikmiş fatura hatırlatma bildirimini gönderir.
     *
     * @param bill Fatura entity
     * @param customer Müşteri bilgileri
     */
    @Async("taskExecutor")
    public void sendOverdueBillReminder(Bill bill, CustomerResponse customer) {
        log.info("Sending overdue bill reminder for billId: {}", bill.getId());
        NotificationRequest request = NotificationRequest.builder()
                .recipient(customer.getEmail())
                .subject("Gecikmiş Fatura Bildirimi")
                .body("Sayın " + customer.getName() + ",\n\n" +
                        "Bir faturanızın vadesi geçmiştir.\n" +
                        "Fatura ID: " + bill.getId() + "\n" +
                        "Toplam Tutar: " + bill.getTotalAmount() + "\n" +
                        "Vade Tarihi: " + bill.getDueDate() + "\n\n" +
                        "Lütfen en kısa sürede ödemenizi gerçekleştiriniz.\n" +
                        "Gecikmeden kaynaklanan ek ücretler uygulanabilir.\n" +
                        "İyi günler dileriz.")
                .build();
        notificationServiceClient.sendNotification(request);
    }
}