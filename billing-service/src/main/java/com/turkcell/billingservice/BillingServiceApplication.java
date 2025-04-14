package com.turkcell.billingservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication(exclude = {ManagementWebSecurityAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.turkcell.billingservice.clients")
@EnableRetry
@EnableCaching
@EnableAsync
@EnableScheduling
@OpenAPIDefinition(
    info = @Info(
        title = "Billing Service API",
        version = "1.0",
        description = "API for managing billing and payment operations in Telecom CRM"
    ),
    security = @SecurityRequirement(name = "bearerAuth")
)
@ComponentScan(basePackages = "com.turkcell.billingservice")
@EntityScan("com.turkcell.billingservice.domain.entities")
@EnableJpaRepositories("com.turkcell.billingservice.repositories")
@EnableMethodSecurity
public class BillingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BillingServiceApplication.class, args);
    }
} 