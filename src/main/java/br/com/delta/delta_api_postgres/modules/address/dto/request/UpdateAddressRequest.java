package br.com.delta.delta_api_postgres.modules.address.dto.request;

public record UpdateAddressRequest(
        Integer regionId,
        String cep,
        String city,
        String state
) {
}
