package com.turkcell.billingservice.domain.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    @NotBlank(message = "Recipient cannot be empty")
    private String recipient;
    
    @NotBlank(message = "Subject cannot be empty")
    private String subject;
    
    @NotBlank(message = "Body cannot be empty")
    private String body;
} 