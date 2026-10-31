package br.com.delta.delta_api_postgres.modules.address.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.address.dto.io.AddressIO;
import br.com.delta.delta_api_postgres.modules.address.entity.Address;
import br.com.delta.delta_api_postgres.modules.address.mapper.AddressMapper;
import br.com.delta.delta_api_postgres.modules.address.repository.AddressRepository;
import br.com.delta.delta_api_postgres.modules.region.entity.Region;
import br.com.delta.delta_api_postgres.modules.region.repository.MunicipalityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {
    private final AddressRepository addressRepository;
    private final MunicipalityRepository municipalityRepository;
    private final AddressMapper addressMapper;

    public AddressIO create(AddressIO addressIO) {
        Region region = findRegionByCity(addressIO);
        Address address = addressMapper.toEntity(addressIO, region);

        Address savedAddress = addressRepository.save(address);

        return addressMapper.toIo(savedAddress);
    }
    @Transactional(readOnly = true)
    public List<AddressIO> findAll(){
        return addressRepository.findAll().stream().map(addressMapper::toIo).toList();
    }
    @Transactional(readOnly = true)
    public AddressIO findById(Integer id) {
        Address address = addressRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Endereço nao encontrado")
        );

        return addressMapper.toIo(address);
    }
    public AddressIO update(Integer id, AddressIO addressIO) {
        Address address = addressRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Endereço nao encontrado")
        );

        Region region = findRegionByCity(addressIO);

        addressMapper.updateEntity(address, addressIO, region);

        return addressMapper.toIo(addressRepository.save(address));
    }
    private Region findRegionByCity(AddressIO addressIO) {
        if (!"SP".equalsIgnoreCase(addressIO.state().trim())) {
            throw cityNotServed();
        }
        return municipalityRepository
                .findByNameIgnoreCase(addressIO.city().trim())
                .orElseThrow(this::cityNotServed)
                .getRegion();
    }

    private ResponseStatusException cityNotServed() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "O Delta atende apenas cidades de São Paulo cobertas pela Sabesp."
        );
    }
    public void delete(Integer id) {
        Address address = addressRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Endereço nao encontrado")
        );

        addressRepository.delete(address);
    }
}
