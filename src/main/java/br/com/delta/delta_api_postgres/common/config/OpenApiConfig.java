package br.com.delta.delta_api_postgres.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@OpenAPIDefinition(
        security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"),
        info = @Info(
                title = "Delta API",
                version = "1.0.0",
                description = "Documentacao dos endpoints da Delta API"
        )
)
public class OpenApiConfig {
}
