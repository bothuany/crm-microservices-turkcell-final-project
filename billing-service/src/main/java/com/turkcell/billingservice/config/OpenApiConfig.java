package com.turkcell.billingservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port}")
    private String serverPort;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Bean
    public OpenAPI billingServiceOpenAPI() {
        Server devServer = new Server()
                .url("http://localhost:" + serverPort + contextPath)
                .description("Development server");

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

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
} 