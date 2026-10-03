package br.com.delta.delta_api_postgres.modules.address.dto.io;

import java.math.BigDecimal;

public record AddressIO(
    Integer id,
    Integer regionId,
    String cep,
    String city,
    String state,
    BigDecimal latitude,
    BigDecimal longitude
) {
}
