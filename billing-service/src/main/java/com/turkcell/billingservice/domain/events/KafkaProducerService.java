package com.turkcell.billingservice.domain.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka'ya event göndermek için kullanılan servis.
 * Bu servis, sistemde oluşan event'leri Kafka'ya göndererek diğer mikroservislerin bu event'leri dinlemesini sağlar.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Bir event'i Kafka'ya gönderir.
     *
     * @param topic Event'in gönderileceği topic
     * @param event Gönderilecek event
     * @param <T> Event tipi
     */
    public <T extends BaseEvent> void sendEvent(String topic, T event) {
        try {
            log.info("Sending event to topic {}: {}", topic, event);
            kafkaTemplate.send(topic, event.getEventId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send event to topic {}: {}", topic, ex.getMessage());
                        } else {
                            log.info("Successfully sent event to topic {}: {}", topic, event.getEventId());
                        }
                    });
        } catch (Exception ex) {
            log.error("Error sending event to topic {}: {}", topic, ex.getMessage());
        }
    }
} 