package br.com.delta.delta_api_postgres.modules.address.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

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
        String state,
        @DecimalMin(value = "-90.000000", message = "Latitude mínima é -90")
        @DecimalMax(value = "90.000000", message = "Latitude máxima é 90")
        BigDecimal latitude,
        @DecimalMin(value = "-180.000000", message = "Longitude mínima é -180")
        @DecimalMax(value = "180.000000", message = "Longitude máxima é 180")
        BigDecimal longitude
) {
}
