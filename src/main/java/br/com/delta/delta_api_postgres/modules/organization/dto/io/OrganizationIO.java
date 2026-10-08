package br.com.delta.delta_api_postgres.modules.organization.dto.io;

import br.com.delta.delta_api_postgres.modules.organization.enums.OrganizationSegment;

import java.time.LocalDate;

public record OrganizationIO(
        Integer id,
        String corporateName,
        String tradeName,
        String cnpj,
        OrganizationSegment businessSegment,
        Integer declaredUnitCount,
        LocalDate registrationDate
) {
}
