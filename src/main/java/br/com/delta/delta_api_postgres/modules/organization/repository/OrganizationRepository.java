package br.com.delta.delta_api_postgres.modules.organization.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

import br.com.delta.delta_api_postgres.modules.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Integer> {
    @Query("select distinct o from Organization o where exists (select u.id from UserProperty u where u.property.organization = o and u.userId = :userId)")
    List<Organization> findForUser(@Param("userId") Integer userId);

    boolean existsByCnpj(String cnpj);
}
