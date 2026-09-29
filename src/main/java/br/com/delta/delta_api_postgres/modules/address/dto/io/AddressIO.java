package br.com.delta.delta_api_postgres.modules.address.dto.io;

public record AddressIO(
    Integer id,
    Integer regionId,
    String cep,
    String city,
    String state
) {
}
