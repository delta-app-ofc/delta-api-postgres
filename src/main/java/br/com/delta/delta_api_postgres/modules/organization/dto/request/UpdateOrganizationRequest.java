package br.com.delta.delta_api_postgres.modules.organization.dto.request;

import br.com.delta.delta_api_postgres.modules.organization.enums.OrganizationSegment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateOrganizationRequest(
        @NotBlank(message = "Razão social é obrigatória")
        @Size(max = 150, message = "Razão social deve possuir no máximo 150 caracteres")
        String corporateName,

        @NotBlank(message = "Nome fantasia é obrigatório")
        @Size(max = 150, message = "Nome fantasia deve possuir no máximo 150 caracteres")
        String tradeName,

        @NotBlank(message = "CNPJ é obrigatório")
        @Pattern(
                regexp = "^[0-9]{14}$",
                message = "CNPJ deve possuir exatamente 14 números"
        )
        String cnpj,

        @NotNull(message = "Segmento de negócio é obrigatório")
        OrganizationSegment businessSegment,

        @Positive(message = "Quantidade declarada de unidades deve ser maior que zero")
        Integer declaredUnitCount
) {
}
