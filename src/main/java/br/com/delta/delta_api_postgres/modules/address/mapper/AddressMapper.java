package br.com.delta.delta_api_postgres.modules.address.mapper;

import br.com.delta.delta_api_postgres.modules.address.dto.io.AddressIO;
import br.com.delta.delta_api_postgres.modules.address.dto.request.CreateAddressRequest;
import br.com.delta.delta_api_postgres.modules.address.dto.request.UpdateAddressRequest;
import br.com.delta.delta_api_postgres.modules.address.entity.Address;
import br.com.delta.delta_api_postgres.modules.region.entity.Region;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {
    public AddressIO toIo(Address address){
        return new AddressIO(
                address.getId(),
                address.getRegion().getId(),
                address.getCep(),
                address.getCity(),
                address.getState()
        );
    }

    public Address toEntity(AddressIO addressIO, Region region){
        return new Address(
                addressIO.id(),
                region,
                addressIO.cep(),
                addressIO.city(),
                addressIO.state()
        );
    }

    public AddressIO fromCreateRequest(CreateAddressRequest createAddressRequest){
        return new AddressIO(
                null,
                null,
                createAddressRequest.cep(),
                createAddressRequest.city(),
                createAddressRequest.state()
        );
    }

    public AddressIO fromUpdateRequest(Integer id, UpdateAddressRequest updateAddressRequest){
        return new AddressIO(
                id,
                updateAddressRequest.regionId(),
                updateAddressRequest.cep(),
                updateAddressRequest.city(),
                updateAddressRequest.state()
        );
    }

    public void updateEntity(Address address, AddressIO io, Region region){
        address.setRegion(region);
        address.setCep(io.cep());
        address.setCity(io.city());
        address.setState(io.state());
    }
}
