package br.com.delta.delta_api_postgres.modules.property_goal.swagger;

import br.com.delta.delta_api_postgres.modules.property_goal.dto.io.PropertyGoalIO;
import br.com.delta.delta_api_postgres.modules.property_goal.dto.request.UpdatePropertyGoalRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Metas de consumo", description = "Consulta e atualização da meta mensal de consumo de uma instalação, em litros.")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Sessão ausente, inválida ou expirada"),
        @ApiResponse(responseCode = "403", description = "Usuário sem acesso à instalação")
})
public interface PropertyGoalSwagger {

    @Operation(summary = "Consultar meta mensal", description = "Retorna a meta mensal em litros configurada para a instalação. A consulta é restrita às instalações acessíveis ao usuário autenticado.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Meta consultada"), @ApiResponse(responseCode = "404", description = "Instalação ou meta não encontrada")})
    ResponseEntity<PropertyGoalIO> find(@Parameter(description = "Identificador da instalação", example = "152") Integer propertyId);

    @Operation(summary = "Criar ou atualizar meta mensal", description = "Salva a meta mensal de consumo da instalação em litros. O valor deve ser maior que zero e não pode exceder 999.999.999,99 litros.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Meta salva"), @ApiResponse(responseCode = "400", description = "Valor ausente ou fora dos limites permitidos"), @ApiResponse(responseCode = "404", description = "Instalação não encontrada")})
    ResponseEntity<PropertyGoalIO> save(@Parameter(description = "Identificador da instalação", example = "152") Integer propertyId, @Valid @RequestBody UpdatePropertyGoalRequest request);
}
