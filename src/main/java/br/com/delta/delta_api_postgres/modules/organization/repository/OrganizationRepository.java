package br.com.delta.delta_api_postgres.modules.organization.repository;

import br.com.delta.delta_api_postgres.modules.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Integer> {
    boolean existsByCnpj(String cnpj);
}
