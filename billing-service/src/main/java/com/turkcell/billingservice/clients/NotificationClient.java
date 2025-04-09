package com.turkcell.billingservice.clients;

import com.turkcell.billingservice.dtos.requests.SendNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationClient {
    @PostMapping("/api/v1/notifications")
    void sendNotification(@RequestBody SendNotificationRequest request);
} 