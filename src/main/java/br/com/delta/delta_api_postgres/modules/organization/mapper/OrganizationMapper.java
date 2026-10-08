package br.com.delta.delta_api_postgres.modules.organization.mapper;

import br.com.delta.delta_api_postgres.modules.organization.dto.io.OrganizationIO;
import br.com.delta.delta_api_postgres.modules.organization.dto.request.CreateOrganizationRequest;
import br.com.delta.delta_api_postgres.modules.organization.dto.request.UpdateOrganizationRequest;
import br.com.delta.delta_api_postgres.modules.organization.entity.Organization;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper {
    public OrganizationIO toIO(Organization organization) {
        return new OrganizationIO(
                organization.getId(),
                organization.getCorporateName(),
                organization.getTradeName(),
                organization.getCnpj(),
                organization.getBusinessSegment(),
                organization.getDeclaredUnitCount(),
                organization.getRegistrationDate()
        );
    }

    public Organization toEntity(OrganizationIO io) {
        return new Organization(
                io.id(),
                io.corporateName(),
                io.tradeName(),
                io.cnpj(),
                io.businessSegment(),
                io.declaredUnitCount(),
                io.registrationDate()
        );
    }

    public OrganizationIO fromCreateRequest(CreateOrganizationRequest request) {
        return new OrganizationIO(
                null,
                request.corporateName(),
                request.tradeName(),
                request.cnpj(),
                request.businessSegment(),
                request.declaredUnitCount(),
                null
        );
    }

    public OrganizationIO fromUpdateRequest(Integer id, UpdateOrganizationRequest request) {
        return new OrganizationIO(
                id,
                request.corporateName(),
                request.tradeName(),
                request.cnpj(),
                request.businessSegment(),
                request.declaredUnitCount(),
                null
        );
    }

    public void updateEntity(Organization organization, OrganizationIO io) {
        organization.setCorporateName(io.corporateName());
        organization.setTradeName(io.tradeName());
        organization.setCnpj(io.cnpj());
        organization.setBusinessSegment(io.businessSegment());
        organization.setDeclaredUnitCount(io.declaredUnitCount());
    }
}
