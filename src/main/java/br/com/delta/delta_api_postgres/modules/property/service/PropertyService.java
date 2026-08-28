package br.com.delta.delta_api_postgres.modules.property.service;

import br.com.delta.delta_api_postgres.modules.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PropertyService {
    private final PropertyRepository propertyRepository;


}
