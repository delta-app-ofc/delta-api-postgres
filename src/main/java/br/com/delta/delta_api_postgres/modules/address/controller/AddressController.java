package br.com.delta.delta_api_postgres.modules.address.controller;

import br.com.delta.delta_api_postgres.modules.address.dto.io.AddressIO;
import br.com.delta.delta_api_postgres.modules.address.dto.request.CreateAddressRequest;
import br.com.delta.delta_api_postgres.modules.address.dto.request.UpdateAddressRequest;
import br.com.delta.delta_api_postgres.modules.address.mapper.AddressMapper;
import br.com.delta.delta_api_postgres.modules.address.service.AddressService;
import br.com.delta.delta_api_postgres.modules.address.swagger.AddressSwagger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delta/address")
@RequiredArgsConstructor
public class AddressController implements AddressSwagger {
    private final AddressService addressService;
    private final AddressMapper addressMapper;

    @Override
    @PostMapping
    public ResponseEntity<AddressIO> create(
            @Valid @RequestBody CreateAddressRequest request){

        AddressIO response = addressService.create(
                addressMapper.fromCreateRequest(request)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @Override
    @GetMapping
    public ResponseEntity<List<AddressIO>> findAll(){
        List<AddressIO> response = addressService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @Override
    @GetMapping("/{id}")
    public ResponseEntity<AddressIO> findById(@PathVariable Integer id){
        AddressIO response = addressService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @Override
    @PutMapping("/{id}")
    public ResponseEntity<AddressIO> update(@PathVariable Integer id, @RequestBody @Valid UpdateAddressRequest updateAddressRequest){
        AddressIO response = addressService.update(id, addressMapper.fromUpdateRequest(id, updateAddressRequest));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id){
        addressService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
