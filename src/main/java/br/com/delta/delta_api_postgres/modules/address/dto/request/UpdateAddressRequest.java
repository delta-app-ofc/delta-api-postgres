package br.com.delta.delta_api_postgres.modules.address.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateAddressRequest(
        @NotNull(message = "Region ID é obrigatório")
        Integer regionId,
        @NotBlank(message = "CEP é obrigatório")
        @Pattern(
                regexp = "^[0-9]{8}$",
                message = "CEP deve possuir exatamente 8 números"
        )
        String cep,
        @NotBlank(message = "Cidade é obrigatória")
        @Size(max = 60, message = "Cidade deve possuir no máximo 60 caracteres")
        String city,
        @NotBlank(message = "Estado é obrigatório")
        @Size(max = 30, message = "Estado deve possuir no máximo 30 caracteres")
        String state
) {
}
