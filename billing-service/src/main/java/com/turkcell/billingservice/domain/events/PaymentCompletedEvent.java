package com.turkcell.billingservice.domain.events;

import com.turkcell.billingservice.domain.enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Ödeme tamamlandığında tetiklenen event.
 * Bu event, bir ödeme işlemi başarıyla tamamlandığında sistemin diğer bileşenlerini bilgilendirir.
 */
@Getter
@Setter
public class PaymentCompletedEvent extends BaseEvent {
    /**
     * Ödemenin yapıldığı fatura ID'si.
     */
    private UUID billId;

    /**
     * Ödemenin yapıldığı müşteri ID'si.
     */
    private UUID customerId;

    /**
     * Ödeme tutarı.
     */
    private BigDecimal amount;

    /**
     * Ödeme yöntemi.
     */
    private PaymentMethod paymentMethod;

    /**
     * Yeni bir PaymentCompletedEvent oluşturur.
     *
     * @param paymentId Tamamlanan ödemenin ID'si
     * @param billId Ödemenin yapıldığı fatura ID'si
     * @param customerId Ödemenin yapıldığı müşteri ID'si
     * @param amount Ödeme tutarı
     * @param paymentMethod Ödeme yöntemi
     */
    public PaymentCompletedEvent(UUID paymentId, UUID billId, UUID customerId, BigDecimal amount, PaymentMethod paymentMethod) {
        super("PAYMENT_COMPLETED", paymentId);
        this.billId = billId;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }
} 