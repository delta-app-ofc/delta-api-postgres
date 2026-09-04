package br.com.delta.delta_api_postgres.modules.address.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.address.dto.io.AddressIO;
import br.com.delta.delta_api_postgres.modules.address.dto.request.CreateAddressRequest;
import br.com.delta.delta_api_postgres.modules.address.entity.Address;
import br.com.delta.delta_api_postgres.modules.address.mapper.AddressMapper;
import br.com.delta.delta_api_postgres.modules.address.repository.AddressRepository;
import br.com.delta.delta_api_postgres.modules.region.entity.Region;
import br.com.delta.delta_api_postgres.modules.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;
    private final RegionRepository regionRepository;
    private final AddressMapper addressMapper;

    public AddressIO create(AddressIO addressIO) {
            Region region = regionRepository.findById(addressIO.regionId()).orElseThrow(
                    () -> new ResourceNotFoundException("Região nao encontrada")
            );

            Address address = addressMapper.toEntity(addressIO, region);

            Address savedAddress = addressRepository.save(address);

            return addressMapper.toIo(savedAddress);
        }
}
