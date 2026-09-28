package br.com.delta.delta_api_postgres.modules.property.dto.request;

import br.com.delta.delta_api_postgres.modules.property.enums.PropertyClassification;
import br.com.delta.delta_api_postgres.modules.property.enums.PropertyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreatePropertyRequest(
        @Schema(
                description = "ID do usuário ativo que será associado à propriedade",
                example = "1"
        )
        @NotNull @Positive
        Integer userId,

        @Schema(
                description = "Nome da propriedade",
                example = "Casa principal"
        )
        @NotBlank @Size(max = 100)
        String name,

        @Schema(
                description = "Tipo da propriedade",
                example = "CASA"
        )
        @NotNull
        PropertyType type,

        @Schema(
                description = "Classificação usada para determinar a categoria tarifária",
                example = "RESIDENCIAL_NORMAL"
        )
        @NotNull
        PropertyClassification classification,

        @Schema(
                description = "ID do endereço previamente cadastrado",
                example = "10"
        )
        @NotNull
        @Positive
        Integer addressId
) {
}
