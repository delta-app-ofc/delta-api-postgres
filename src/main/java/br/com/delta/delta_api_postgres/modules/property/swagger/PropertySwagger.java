package br.com.delta.delta_api_postgres.modules.property.swagger;

import br.com.delta.delta_api_postgres.modules.property.dto.io.PropertyIO;
import br.com.delta.delta_api_postgres.modules.property.dto.io.WaterCostIO;
import br.com.delta.delta_api_postgres.modules.property.dto.request.CreatePropertyRequest;
import br.com.delta.delta_api_postgres.modules.property.dto.request.UpdatePropertyRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Tag(
        name = "Properties",
        description = "Operações para gerenciamento de propriedades"
)
public interface PropertySwagger {

    @Operation(summary = "Cadastrar propriedade")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Propriedade cadastrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    })
    ResponseEntity<PropertyIO> create(CreatePropertyRequest request);

    @Operation(summary = "Listar propriedades")
    @ApiResponse(responseCode = "200", description = "Propriedades encontradas")
    ResponseEntity<List<PropertyIO>> findAll();

    @Operation(summary = "Consultar propriedade por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Propriedade encontrada"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada")
    })
    ResponseEntity<PropertyIO> findById(Integer id);

    @Operation(
            summary = "Calcular custo estimado de água",
            description = "Calcula o custo estimado do consumo de água de uma propriedade, " +
                    "considerando a tarifa de sua região vigente na data de referência. " +
                    "Quando a data não é informada, é utilizada a data atual do banco de dados. " +
                    "A operação apenas calcula a estimativa e não cria uma fatura."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Custo estimado calculado"),
            @ApiResponse(responseCode = "400", description = "Parâmetros ausentes ou inválidos"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno durante o cálculo")
    })
    ResponseEntity<WaterCostIO> calculateWaterCost(
            @Parameter(
                    description = "ID da propriedade",
                    required = true,
                    example = "1"
            )
            Integer propertyId,
            @Parameter(
                    description = "Consumo de água em metros cúbicos",
                    required = true,
                    example = "15.5"
            )
            BigDecimal consumptionM3,
            @Parameter(
                    description = "Data usada para localizar a tarifa vigente, no formato AAAA-MM-DD",
                    example = "2026-09-16"
            )
            LocalDate referenceDate
    );

    @Operation(summary = "Atualizar propriedade")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Propriedade atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Propriedade ou endereço não encontrado")
    })
    ResponseEntity<PropertyIO> update(
            Integer id,
            UpdatePropertyRequest request
    );

    @Operation(summary = "Excluir propriedade")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Propriedade excluída"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada"),
            @ApiResponse(responseCode = "409", description = "Propriedade vinculada a outro recurso")
    })
    ResponseEntity<Void> delete(Integer id);
}
