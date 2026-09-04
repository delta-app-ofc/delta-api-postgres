package br.com.delta.delta_api_postgres.modules.address.dto.request;

public record UpdateAddressRequest(
        String cep,
        String city,
        String state
) {
}
