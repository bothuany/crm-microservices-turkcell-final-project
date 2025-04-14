package com.turkcell.billingservice.services;

import com.turkcell.billingservice.domain.exceptions.BillingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Ödeme ağ geçidi işlemlerini yönetir.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayService {

    @Value("${stripe.api.key:dummy-key}")
    private String stripeApiKey;

    /**
     * Ödeme işlemini gerçekleştirir.
     * Not: Bu uygulama gerçek bir ödeme ağ geçidi entegrasyonu içermemektedir.
     * Ödeme başarılı kabul edilip rastgele bir işlem ID döndürülmektedir.
     *
     * @param amount Ödeme tutarı
     * @param paymentMethod Ödeme yöntemi
     * @return İşlem ID'si
     */
    public String processPayment(Double amount, String paymentMethod) {
        log.info("Processing payment of amount: {} with method: {}", amount, paymentMethod);
        
        try {
            // Stripe API kullanımı burada simüle edilmiştir
            log.info("Payment processed successfully with Stripe API Key: {}", stripeApiKey);
            
            // Rastgele bir işlem ID döndürülüyor
            String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            log.info("Transaction ID generated: {}", transactionId);
            return transactionId;
        } catch (Exception e) {
            log.error("Error while processing payment: {}", e.getMessage());
            throw new BillingException("Payment processing failed: " + e.getMessage());
        }
    }
    
    /**
     * İade işlemini gerçekleştirir.
     * Not: Bu uygulama gerçek bir ödeme ağ geçidi entegrasyonu içermemektedir.
     * İade başarılı kabul edilip rastgele bir işlem ID döndürülmektedir.
     *
     * @param amount İade tutarı
     * @param paymentMethod Ödeme yöntemi
     * @return İşlem ID'si
     */
    public String processRefund(Double amount, String paymentMethod) {
        log.info("Processing refund of amount: {} with method: {}", amount, paymentMethod);
        
        try {
            // Stripe API kullanımı burada simüle edilmiştir
            log.info("Refund processed successfully with Stripe API Key: {}", stripeApiKey);
            
            // Rastgele bir işlem ID döndürülüyor
            String refundTransactionId = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            log.info("Refund Transaction ID generated: {}", refundTransactionId);
            return refundTransactionId;
        } catch (Exception e) {
            log.error("Error while processing refund: {}", e.getMessage());
            throw new BillingException("Refund processing failed: " + e.getMessage());
        }
    }
} 