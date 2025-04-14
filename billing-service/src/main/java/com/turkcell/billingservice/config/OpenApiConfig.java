package com.turkcell.billingservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * OpenAPI (Swagger) konfigürasyonu.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    public OpenAPI billingServiceOpenAPI() {
        Server devServer = new Server()
                .url("http://localhost:" + serverPort + contextPath)
                .description("Development server");
                
        Server stagingServer = new Server()
                .url("https://staging-api.turkcell.com.tr/billing")
                .description("Staging server");
                
        Server productionServer = new Server()
                .url("https://api.turkcell.com.tr/billing")
                .description("Production server");

        Contact contact = new Contact()
                .name("Turkcell CRM Team")
                .email("crm@turkcell.com.tr")
                .url("https://www.turkcell.com.tr");

        License license = new License()
                .name("Apache 2.0")
                .url("http://www.apache.org/licenses/LICENSE-2.0.html");

        Info info = new Info()
                .title("Billing Service API")
                .version("1.0")
                .contact(contact)
                .description("This API exposes endpoints for managing bills, payments, and refunds.")
                .termsOfService("https://www.turkcell.com.tr/terms")
                .license(license);
                
        // API kategorilerini tanımlama
        Tag billsTag = new Tag().name("Bills").description("Operations about bills");
        Tag paymentsTag = new Tag().name("Payments").description("Operations about payments");
        Tag refundsTag = new Tag().name("Refunds").description("Operations about refunds");
        Tag reportsTag = new Tag().name("Reports").description("Operations about reports and analytics");

        // Güvenlik şeması oluşturma
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token}\"");

        return new OpenAPI()
                .info(info)
                .servers(Arrays.asList(devServer, stagingServer, productionServer))
                .tags(Arrays.asList(billsTag, paymentsTag, refundsTag, reportsTag))
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
} 