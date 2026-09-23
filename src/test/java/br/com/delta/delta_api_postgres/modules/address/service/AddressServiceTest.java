package br.com.delta.delta_api_postgres.modules.address.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.address.dto.io.AddressIO;
import br.com.delta.delta_api_postgres.modules.address.entity.Address;
import br.com.delta.delta_api_postgres.modules.address.mapper.AddressMapper;
import br.com.delta.delta_api_postgres.modules.address.repository.AddressRepository;
import br.com.delta.delta_api_postgres.modules.region.entity.Region;
import br.com.delta.delta_api_postgres.modules.region.repository.RegionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    private static final Integer ADDRESS_ID = 1;
    private static final Integer REGION_ID = 10;
    private static final String CEP = "01310100";
    private static final String CITY = "São Paulo";
    private static final String STATE = "São Paulo";

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressService addressService;

    @Test
    void create_quandoDadosValidos_deveSalvarERetornarEndereco() {
        // Arrange
        AddressIO input = inputAddressIO();
        Region region = new Region();
        Address address = address(null, region);
        Address savedAddress = address(ADDRESS_ID, region);
        AddressIO expected = savedAddressIO();

        when(regionRepository.findById(REGION_ID)).thenReturn(Optional.of(region));
        when(addressMapper.toEntity(input, region)).thenReturn(address);
        when(addressRepository.save(address)).thenReturn(savedAddress);
        when(addressMapper.toIo(savedAddress)).thenReturn(expected);

        // Act
        AddressIO result = addressService.create(input);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(addressRepository).save(address);
        verify(addressMapper).toIo(savedAddress);
    }

    @Test
    void create_quandoRegiaoNaoExiste_deveLancarResourceNotFoundException() {
        AddressIO input = inputAddressIO();
        when(regionRepository.findById(REGION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.create(input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Região nao encontrada");

        verify(addressRepository, never()).save(any());
        verifyNoInteractions(addressMapper);
    }

    @Test
    void findAll_quandoExistemEnderecos_deveRetornarTodosMapeados() {
        Region region = new Region();
        Address firstAddress = address(ADDRESS_ID, region);
        Address secondAddress = new Address(2, region, "20040002", "Rio de Janeiro", "Rio de Janeiro");

        AddressIO firstIO = savedAddressIO();
        AddressIO secondIO = new AddressIO(2, REGION_ID, "20040002", "Rio de Janeiro", "Rio de Janeiro");

        when(addressRepository.findAll()).thenReturn(List.of(firstAddress, secondAddress));
        when(addressMapper.toIo(firstAddress)).thenReturn(firstIO);
        when(addressMapper.toIo(secondAddress)).thenReturn(secondIO);

        List<AddressIO> result = addressService.findAll();

        assertThat(result).containsExactly(firstIO, secondIO);
        verify(addressMapper).toIo(firstAddress);
        verify(addressMapper).toIo(secondAddress);
    }

    @Test
    void findAll_quandoNaoExistemEnderecos_deveRetornarListaVazia() {
        when(addressRepository.findAll()).thenReturn(List.of());

        List<AddressIO> result = addressService.findAll();

        assertThat(result).isEmpty();
        verifyNoInteractions(addressMapper);
    }

    @Test
    void findById_quandoEnderecoExiste_deveRetornarEnderecoMapeado() {
        Address address = address(ADDRESS_ID, new Region());
        AddressIO expected = savedAddressIO();

        when(addressRepository.findById(ADDRESS_ID)).thenReturn(Optional.of(address));
        when(addressMapper.toIo(address)).thenReturn(expected);

        AddressIO result = addressService.findById(ADDRESS_ID);

        assertThat(result).isEqualTo(expected);
        verify(addressMapper).toIo(address);
    }

    @Test
    void findById_quandoEnderecoNaoExiste_deveLancarResourceNotFoundException() {
        when(addressRepository.findById(ADDRESS_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.findById(ADDRESS_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Endereço nao encontrado");

        verifyNoInteractions(addressMapper);
    }

    @Test
    void update_quandoDadosValidos_deveAtualizarERetornarEndereco() {
        AddressIO input = inputAddressIO();
        Region region = new Region();
        Address address = address(ADDRESS_ID, region);
        AddressIO expected = savedAddressIO();

        when(addressRepository.findById(ADDRESS_ID)).thenReturn(Optional.of(address));
        when(regionRepository.findById(REGION_ID)).thenReturn(Optional.of(region));
        when(addressRepository.save(address)).thenReturn(address);
        when(addressMapper.toIo(address)).thenReturn(expected);

        AddressIO result = addressService.update(ADDRESS_ID, input);

        assertThat(result).isEqualTo(expected);
        verify(addressMapper).updateEntity(address, input, region);
        verify(addressRepository).save(address);
    }

    @Test
    void update_quandoEnderecoNaoExiste_deveLancarResourceNotFoundException() {
        AddressIO input = inputAddressIO();
        when(addressRepository.findById(ADDRESS_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.update(ADDRESS_ID, input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Endereço nao encontrado");

        verify(addressRepository, never()).save(any());
        verifyNoInteractions(regionRepository, addressMapper);
    }

    @Test
    void update_quandoRegiaoNaoExiste_deveLancarResourceNotFoundException() {
        AddressIO input = inputAddressIO();
        Address address = address(ADDRESS_ID, new Region());

        when(addressRepository.findById(ADDRESS_ID)).thenReturn(Optional.of(address));
        when(regionRepository.findById(REGION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.update(ADDRESS_ID, input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Região nao encontrada");

        verify(addressRepository, never()).save(any());
        verifyNoInteractions(addressMapper);
    }

    @Test
    void delete_quandoEnderecoExiste_deveExcluirEndereco() {
        Address address = address(ADDRESS_ID, new Region());
        when(addressRepository.findById(ADDRESS_ID)).thenReturn(Optional.of(address));

        addressService.delete(ADDRESS_ID);

        verify(addressRepository).delete(address);
    }

    @Test
    void delete_quandoEnderecoNaoExiste_deveLancarResourceNotFoundException() {
        when(addressRepository.findById(ADDRESS_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.delete(ADDRESS_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Endereço nao encontrado");

        verify(addressRepository, never()).delete(any());
    }

    private AddressIO inputAddressIO() {
        return new AddressIO(null, REGION_ID, CEP, CITY, STATE);
    }

    private AddressIO savedAddressIO() {
        return new AddressIO(ADDRESS_ID, REGION_ID, CEP, CITY, STATE);
    }

    private Address address(Integer id, Region region) {
        return new Address(id, region, CEP, CITY, STATE);
    }
}
