package br.com.delta.delta_api_postgres.modules.address.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

import br.com.delta.delta_api_postgres.modules.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Integer> {
    @Query("select distinct a from Address a where exists (select u.id from UserProperty u where u.property.address = a and u.userId = :userId)")
    List<Address> findForUser(@Param("userId") Integer userId);

}
