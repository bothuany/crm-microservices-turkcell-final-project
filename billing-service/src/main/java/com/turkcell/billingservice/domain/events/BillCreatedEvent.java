package com.turkcell.billingservice.domain.events;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Fatura oluşturulduğunda tetiklenen event.
 * Bu event, yeni bir fatura oluşturulduğunda sistemin diğer bileşenlerini bilgilendirir.
 */
@Getter
@Setter
public class BillCreatedEvent extends BaseEvent {
    /**
     * Oluşturulan faturanın müşteri ID'si.
     */
    private UUID customerId;

    /**
     * Oluşturulan faturanın tutarı.
     */
    private BigDecimal amount;

    /**
     * Oluşturulan faturanın son ödeme tarihi.
     */
    private String dueDate;

    /**
     * Yeni bir BillCreatedEvent oluşturur.
     *
     * @param billId Oluşturulan faturanın ID'si
     * @param customerId Faturanın müşteri ID'si
     * @param amount Fatura tutarı
     * @param dueDate Son ödeme tarihi
     */
    public BillCreatedEvent(UUID billId, UUID customerId, BigDecimal amount, String dueDate) {
        super("BILL_CREATED", billId);
        this.customerId = customerId;
        this.amount = amount;
        this.dueDate = dueDate;
    }
} 