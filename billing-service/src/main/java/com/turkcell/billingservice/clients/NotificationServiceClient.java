package com.turkcell.billingservice.clients;

import com.turkcell.billingservice.config.FeignClientConfig;
import com.turkcell.billingservice.domain.dtos.requests.NotificationRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * NotificationServiceClient - Notification service client for sending notifications.
 */
@FeignClient(
    name = "notification-service",
    path = "/api/v1/notifications",
    url = "${services.notification-service.url:#{null}}",
    primary = false,
    configuration = FeignClientConfig.class
)
public interface NotificationServiceClient {
    /**
     * Send notification
     * @param request notification request
     * @return response entity
     */
    @PostMapping
    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendNotificationFallback")
    ResponseEntity<Object> sendNotification(@RequestBody NotificationRequest request);
    
    /**
     * Fallback method for sendNotification
     * @param request notification request
     * @param exception exception that occurred
     * @return default response entity
     */
    default ResponseEntity<Object> sendNotificationFallback(NotificationRequest request, Exception exception) {
        System.err.println("Fallback for sendNotification to recipient: " + request.getRecipient() + 
                ", subject: " + request.getSubject() + ", error: " + exception.getMessage());
        System.err.println("Notification not sent, needs to be handled by alternative mechanism");
        return ResponseEntity.ok().build();
    }
} 