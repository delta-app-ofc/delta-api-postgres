package br.com.delta.delta_api_postgres.modules.region_rate.swagger;

import br.com.delta.delta_api_postgres.modules.region_rate.dto.io.RegionRateIO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(
        name = "Region Rates",
        description = "Operações para consulta de tarifas por região"
)
public interface RegionRateSwagger {

    @Operation(summary = "Consultar tarifas por região")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarifas da região encontradas"),
            @ApiResponse(responseCode = "404", description = "Nenhuma tarifa encontrada para a região")
    })
    ResponseEntity<List<RegionRateIO>> findByRegion(Integer regionId, Boolean last);
}
