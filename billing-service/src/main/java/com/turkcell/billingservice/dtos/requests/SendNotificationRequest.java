package com.turkcell.billingservice.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {
    private String recipient;
    private String subject;
    private String message;
    private String type; // EMAIL, SMS, etc.
} 