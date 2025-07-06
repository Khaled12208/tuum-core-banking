package com.tuum.fsaccountsservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tuum Core Banking API")
                        .description("""
                                ## Core Banking Microservice API
                                
                                This API provides comprehensive banking functionality including:
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Tuum Banking Team")
                                .email("support@tuum.com")
                                .url("https://tuum.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8083/api/v1")
                                .description("Local Development Server"),
                        new Server()
                                .url("http://fs-accounts-service:8083/api/v1")
                                .description("Docker Container Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("IdempotencyKey"))
                .components(new Components()
                        .addSecuritySchemes("IdempotencyKey", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("Idempotency-Key")
                                .description("Unique key to prevent duplicate processing of requests")));
    }
} 