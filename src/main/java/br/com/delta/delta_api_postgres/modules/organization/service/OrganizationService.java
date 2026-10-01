package br.com.delta.delta_api_postgres.modules.organization.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceAlreadyExistsException;
import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.organization.dto.io.OrganizationIO;
import br.com.delta.delta_api_postgres.modules.organization.entity.Organization;
import br.com.delta.delta_api_postgres.modules.organization.mapper.OrganizationMapper;
import br.com.delta.delta_api_postgres.modules.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    public OrganizationIO create(OrganizationIO io) {
        if (organizationRepository.existsByCnpj(io.cnpj())) {
            throw new ResourceAlreadyExistsException(
                    "Já existe uma organização cadastrada com esse CNPJ"
            );
        }

        Organization organization = organizationMapper.toEntity(io);

        Organization saved = organizationRepository.save(organization);

        return organizationMapper.toIO(saved);
    }

    @Transactional(readOnly = true)
    public List<OrganizationIO> findAll() {
        return organizationRepository.findAll().stream().map(organizationMapper::toIO).toList();
    }

    @Transactional(readOnly = true)
    public OrganizationIO findById(Integer id) {
        Organization organization = organizationRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Organização não encontrada")
        );

        return organizationMapper.toIO(organization);
    }

    public OrganizationIO update(Integer id, OrganizationIO io) {
        Organization organization = organizationRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Organização não encontrada")
        );

        if (!organization.getCnpj().equals(io.cnpj()) && organizationRepository.existsByCnpj(io.cnpj())) {
            throw new ResourceAlreadyExistsException(
                    "Já existe uma organização cadastrada com esse CNPJ"
            );
        }

        organizationMapper.updateEntity(organization, io);

        Organization updated = organizationRepository.save(organization);

        return organizationMapper.toIO(updated);
    }

    public void delete(Integer id) {
        Organization organization = organizationRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Organização não encontrada")
        );

        organizationRepository.delete(organization);
    }
}
