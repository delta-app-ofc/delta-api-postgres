package br.com.delta.delta_api_postgres.modules.address.dto.request;

public record CreateAddressRequest(
        String cep,
        String city,
        String state
) {
}
