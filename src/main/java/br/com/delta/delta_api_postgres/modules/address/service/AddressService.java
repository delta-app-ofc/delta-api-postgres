package br.com.delta.delta_api_postgres.modules.address.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.address.dto.io.AddressIO;
import br.com.delta.delta_api_postgres.modules.address.entity.Address;
import br.com.delta.delta_api_postgres.modules.address.mapper.AddressMapper;
import br.com.delta.delta_api_postgres.modules.address.repository.AddressRepository;
import br.com.delta.delta_api_postgres.modules.region.entity.Region;
import br.com.delta.delta_api_postgres.modules.region.repository.RegionRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import br.com.delta.delta_api_postgres.modules.auth.security.UserDataAccess;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {
    private final UserDataAccess access;
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
    @Transactional(readOnly = true)
    public List<AddressIO> findAll(){
        return addressRepository.findForUser(access.userId()).stream().map(addressMapper::toIo).toList();
    }
    @Transactional(readOnly = true)
    public AddressIO findById(Integer id) {
        access.address(id);
        Address address = addressRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Endereço nao encontrado")
        );

        return addressMapper.toIo(address);
    }
    public AddressIO update(Integer id, AddressIO addressIO) {
        access.mutableAddress(id);
        Address address = addressRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Endereço nao encontrado")
        );

        Region region = regionRepository.findById(addressIO.regionId()).orElseThrow(
                () -> new ResourceNotFoundException("Região nao encontrada")
        );

        addressMapper.updateEntity(address, addressIO, region);

        return addressMapper.toIo(addressRepository.save(address));
    }
    public void delete(Integer id) {
        access.deletableAddress(id);
        Address address = addressRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Endereço nao encontrado")
        );

        addressRepository.delete(address);
    }
}
