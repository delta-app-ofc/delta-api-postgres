package br.com.delta.delta_api_postgres.modules.property.controller;

import br.com.delta.delta_api_postgres.modules.address.dto.io.AddressIO;
import br.com.delta.delta_api_postgres.modules.property.dto.io.PropertyIO;
import br.com.delta.delta_api_postgres.modules.property.dto.request.CreatePropertyRequest;
import br.com.delta.delta_api_postgres.modules.property.dto.request.UpdatePropertyRequest;
import br.com.delta.delta_api_postgres.modules.property.mapper.PropertyMapper;
import br.com.delta.delta_api_postgres.modules.property.repository.PropertyRepository;
import br.com.delta.delta_api_postgres.modules.property.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delta/property")
@RequiredArgsConstructor
public class PropertyController {
    private final PropertyService propertyService;
    private final PropertyMapper propertyMapper;

    @PostMapping
    public ResponseEntity<PropertyIO> create(@RequestBody @Valid CreatePropertyRequest request) {
        PropertyIO response = propertyService.create(propertyMapper.fromCreateToIO(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PropertyIO>> findAll() {
        List<PropertyIO> response = propertyService.findAll();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyIO> findById(@PathVariable Integer id) {
        PropertyIO response = propertyService.findById(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyIO> update(@PathVariable Integer id, @RequestBody @Valid
    UpdatePropertyRequest request) {

        PropertyIO response = propertyService.update(id, propertyMapper.fromUpdateRequest(id, request));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        propertyService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
