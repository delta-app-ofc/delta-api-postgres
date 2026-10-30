package br.com.delta.delta_api_postgres.modules.auth.security;

import br.com.delta.delta_api_postgres.common.exception.ResourceAlreadyExistsException;
import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.address.entity.Address;
import br.com.delta.delta_api_postgres.modules.property.entity.Property;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Authorization follows the SQL associations, never a client-supplied user ID alone. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDataAccess {
    private final CurrentUser currentUser;
    private final EntityManager entityManager;

    public Integer userId() { return currentUser.id(); }

    public void requireUser(Integer userId) { currentUser.requireUser(userId); }

    public void property(Integer id) {
        require(count("select count(u) from UserProperty u where u.property.id = :id and u.userId = :userId", id) > 0);
    }

    @Transactional
    public void mutableProperty(Integer id) {
        property(id);
        entityManager.find(Property.class, id, LockModeType.PESSIMISTIC_WRITE);
        conflict(count("select count(u) from UserProperty u where u.property.id = :id and u.userId <> :userId", id) > 0);
    }

    public void address(Integer id) {
        require(count("select count(u) from UserProperty u where u.property.address.id = :id and u.userId = :userId", id) > 0);
    }

    @Transactional
    public void mutableAddress(Integer id) {
        address(id);
        entityManager.find(Address.class, id, LockModeType.PESSIMISTIC_WRITE);
        conflict(count("select count(u) from UserProperty u where u.property.address.id = :id and u.userId <> :userId", id) > 0);
        conflict(count("select count(p) from Property p where p.address.id = :id and not exists "
                + "(select u.id from UserProperty u where u.property = p and u.userId = :userId)", id) > 0);
    }

    /** An unattached address can be consumed by property creation; linked addresses need ownership. */
    @Transactional
    public void assignAddress(Integer id) {
        require(entityManager.find(Address.class, id, LockModeType.PESSIMISTIC_WRITE) != null);
        long references = entityManager.createQuery("select count(p) from Property p where p.address.id = :id", Long.class)
                .setParameter("id", id).getSingleResult();
        if (references > 0) mutableAddress(id);
    }

    @Transactional
    public void deletableAddress(Integer id) {
        mutableAddress(id);
        long references = entityManager.createQuery("select count(p) from Property p where p.address.id = :id", Long.class)
                .setParameter("id", id).getSingleResult();
        if (references > 0) throw new ResourceAlreadyExistsException("Endereço vinculado a imóvel não pode ser excluído.");
    }

    public void bill(Integer id) {
        require(count("select count(b) from LastWaterBill b where b.id = :id and b.userId = :userId", id) > 0);
    }

    public void device(Integer id) {
        require(count("select count(d) from Device d where d.id = :id and exists "
                + "(select u.id from UserProperty u where u.property = d.property and u.userId = :userId)", id) > 0);
    }

    @Transactional
    public void mutableDevice(Integer id) {
        device(id);
        Integer propertyId = entityManager.createQuery("select d.property.id from Device d where d.id = :id", Integer.class)
                .setParameter("id", id).getSingleResult();
        mutableProperty(propertyId);
    }

    public void organization(Integer id) {
        if (id != null) require(count("select count(u) from UserProperty u where u.property.organization.id = :id and u.userId = :userId", id) > 0);
    }

    public void denyOrganizationMutation() {
        throw new AccessDeniedException("O modelo não define permissão de administração de organizações.");
    }

    public void denyUnscopedAnalytics() {
        throw new AccessDeniedException("Esta consulta global não possui escopo de usuário seguro.");
    }

    private long count(String query, Integer id) {
        return entityManager.createQuery(query, Long.class).setParameter("id", id)
                .setParameter("userId", userId()).getSingleResult();
    }

    private void require(boolean allowed) {
        if (!allowed) throw new ResourceNotFoundException("Recurso não encontrado.");
    }

    private void conflict(boolean shared) {
        if (shared) throw new ResourceAlreadyExistsException("Recurso compartilhado com outro usuário; alteração não permitida.");
    }
}
