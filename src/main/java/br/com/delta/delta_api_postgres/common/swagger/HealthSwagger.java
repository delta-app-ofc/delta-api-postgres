package br.com.delta.delta_api_postgres.common.swagger;

import br.com.delta.delta_api_postgres.common.controller.HealthController.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Health", description = "Verificação de disponibilidade da API")
public interface HealthSwagger {

    @Operation(summary = "Consultar saúde da API",
            description = "Verifica a conectividade com PostgreSQL e Redis. Não requer autenticação.",
            security = {})
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PostgreSQL e Redis disponíveis",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = HealthResponse.class),
                            examples = @ExampleObject(value = "{\"status\":\"UP\"}"))),
            @ApiResponse(responseCode = "503", description = "PostgreSQL ou Redis indisponível",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = HealthResponse.class),
                            examples = @ExampleObject(value = "{\"status\":\"DOWN\"}")))
    })
    ResponseEntity<HealthResponse> health();
}
