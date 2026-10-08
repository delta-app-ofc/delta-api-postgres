package br.com.delta.delta_api_postgres.modules.user_property.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceAlreadyExistsException;
import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.property.entity.Property;
import br.com.delta.delta_api_postgres.modules.property.repository.PropertyRepository;
import br.com.delta.delta_api_postgres.modules.user_property.dto.io.UserPropertyIO;
import br.com.delta.delta_api_postgres.modules.user_property.entity.UserProperty;
import br.com.delta.delta_api_postgres.modules.user_property.mapper.UserPropertyMapper;
import br.com.delta.delta_api_postgres.modules.user_property.repository.UserPropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserPropertyService {

    private final UserPropertyRepository userPropertyRepository;
    private final PropertyRepository propertyRepository;
    private final UserPropertyMapper userPropertyMapper;

    public UserPropertyIO create(UserPropertyIO io) {

        if (userPropertyRepository.existsByUserIdAndProperty_Id(
                io.userId(),
                io.propertyId()
        )) {
            throw new ResourceAlreadyExistsException(
                    "Esse imóvel já está associado a esse usuário"
            );
        }

        Property property = propertyRepository.findById(io.propertyId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Imóvel não encontrado")
                );

        UserProperty userProperty = userPropertyMapper.toEntity(io, property);

        UserProperty saved = userPropertyRepository.save(userProperty);

        return userPropertyMapper.toIO(saved);
    }

    @Transactional(readOnly = true)
    public List<UserPropertyIO> findAll(Integer userId) {

        return userPropertyRepository.findByUserId(userId)
                .stream()
                .map(userPropertyMapper::toIO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserPropertyIO findById(
            Integer userId,
            Integer id
    ) {

        UserProperty userProperty = userPropertyRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Associação não encontrada")
                );

        validateOwnership(userProperty, userId);

        return userPropertyMapper.toIO(userProperty);
    }

    public UserPropertyIO update(
            Integer userId,
            Integer id,
            UserPropertyIO io
    ) {

        UserProperty userProperty = userPropertyRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Associação não encontrada")
                );

        validateOwnership(userProperty, userId);

        if (!userProperty.getProperty().getId().equals(io.propertyId())
                && userPropertyRepository.existsByUserIdAndProperty_Id(
                        userId,
                        io.propertyId()
                )) {
            throw new ResourceAlreadyExistsException(
                    "Esse imóvel já está associado a esse usuário"
            );
        }

        Property property = propertyRepository.findById(io.propertyId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Imóvel não encontrado")
                );

        userProperty.setProperty(property);

        return userPropertyMapper.toIO(userProperty);
    }

    public void delete(
            Integer userId,
            Integer id
    ) {

        UserProperty userProperty = userPropertyRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Associação não encontrada")
                );

        validateOwnership(userProperty, userId);

        userPropertyRepository.delete(userProperty);
    }

    private void validateOwnership(
            UserProperty userProperty,
            Integer userId
    ) {

        if (!userProperty.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Associação não encontrada");
        }
    }
}
