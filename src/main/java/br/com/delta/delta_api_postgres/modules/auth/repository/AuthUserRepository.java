package br.com.delta.delta_api_postgres.modules.auth.repository;

import br.com.delta.delta_api_postgres.modules.auth.entity.AuthUser;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Integer> {
    Optional<AuthUser> findByEmail(String email);
    boolean existsByIdAndEnabledTrue(Integer id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from AuthUser u where u.id = :id")
    Optional<AuthUser> lockById(@Param("id") Integer id);
}
