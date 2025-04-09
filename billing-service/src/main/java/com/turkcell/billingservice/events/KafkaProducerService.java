package com.turkcell.billingservice.events;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {
    private static final String TOPIC_BILL_CREATED = "bill-created";

    public void sendBillCreatedEvent(BillCreatedEvent event) {
        // Kafka temporarily disabled
        System.out.println("Kafka temporarily disabled: " + event);
    }
} 