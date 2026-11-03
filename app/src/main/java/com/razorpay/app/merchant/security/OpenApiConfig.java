package com.razorpay.app.merchant.security;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String bearerSchemeName = "bearerAuth";
        final String apiKeySchemeName = "apiKeyAuth";

        return new OpenAPI()
                .info(new Info().title("Razorpay Merchant API").version("1.0.0"))
                .components(new Components()
                        // 1. JWT Bearer Scheme
                        .addSecuritySchemes(bearerSchemeName,
                                new SecurityScheme()
                                        .name(bearerSchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT Bearer token")
                        )
                        // 2. API Key / HTTP Basic Scheme (matches your ApiKeyAuthenticationFilter)
                        .addSecuritySchemes(apiKeySchemeName,
                                new SecurityScheme()
                                        .name(apiKeySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")
                                        .description("Enter Key ID as Username and Key Secret as Password")
                        )
                );
    }
}