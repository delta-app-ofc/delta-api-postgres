package br.com.delta.delta_api_postgres.modules.organization.controller;

import br.com.delta.delta_api_postgres.modules.organization.dto.io.OrganizationIO;
import br.com.delta.delta_api_postgres.modules.organization.dto.request.CreateOrganizationRequest;
import br.com.delta.delta_api_postgres.modules.organization.dto.request.UpdateOrganizationRequest;
import br.com.delta.delta_api_postgres.modules.organization.mapper.OrganizationMapper;
import br.com.delta.delta_api_postgres.modules.organization.service.OrganizationService;
import br.com.delta.delta_api_postgres.modules.organization.swagger.OrganizationSwagger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delta/organizations")
@RequiredArgsConstructor
public class OrganizationController implements OrganizationSwagger {
    private final OrganizationService organizationService;
    private final OrganizationMapper organizationMapper;

    @Override
    @PostMapping
    public ResponseEntity<OrganizationIO> create(
            @Valid @RequestBody CreateOrganizationRequest request) {

        OrganizationIO response = organizationService.create(
                organizationMapper.fromCreateRequest(request)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<OrganizationIO>> findAll() {
        List<OrganizationIO> response = organizationService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<OrganizationIO> findById(@PathVariable Integer id) {
        OrganizationIO response = organizationService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<OrganizationIO> update(
            @PathVariable Integer id,
            @RequestBody @Valid UpdateOrganizationRequest request) {

        OrganizationIO response = organizationService.update(
                id, organizationMapper.fromUpdateRequest(id, request)
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        organizationService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
