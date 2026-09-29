package br.com.delta.delta_api_postgres.modules.last_water_bill.swagger;

import br.com.delta.delta_api_postgres.modules.last_water_bill.dto.io.LastWaterBillIO;
import br.com.delta.delta_api_postgres.modules.last_water_bill.dto.request.CreateLastWaterBillRequest;
import br.com.delta.delta_api_postgres.modules.last_water_bill.dto.request.UpdateLastWaterBillRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(
        name = "Last Water Bills",
        description = "Operações para gerenciamento das últimas faturas de água"
)
public interface LastWaterBillSwagger {

    @Operation(summary = "Cadastrar fatura")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Fatura cadastrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Fatura já cadastrada para esse usuário nesse mês")
    })
    ResponseEntity<LastWaterBillIO> create(CreateLastWaterBillRequest request);

    @Operation(summary = "Listar faturas")
    @ApiResponse(responseCode = "200", description = "Faturas encontradas")
    ResponseEntity<List<LastWaterBillIO>> findAll();

    @Operation(summary = "Consultar fatura por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fatura encontrada"),
            @ApiResponse(responseCode = "404", description = "Fatura não encontrada")
    })
    ResponseEntity<LastWaterBillIO> findById(Integer id);

    @Operation(summary = "Atualizar fatura")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fatura atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Fatura não encontrada"),
            @ApiResponse(responseCode = "409", description = "Fatura já cadastrada para esse usuário nesse mês")
    })
    ResponseEntity<LastWaterBillIO> update(
            Integer id,
            UpdateLastWaterBillRequest request
    );

    @Operation(summary = "Excluir fatura")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Fatura excluída"),
            @ApiResponse(responseCode = "404", description = "Fatura não encontrada")
    })
    ResponseEntity<Void> delete(Integer id);
}
