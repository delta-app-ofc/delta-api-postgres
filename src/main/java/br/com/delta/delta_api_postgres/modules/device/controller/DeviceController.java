package br.com.delta.delta_api_postgres.modules.device.controller;

import br.com.delta.delta_api_postgres.modules.device.dto.io.DeviceIO;
import br.com.delta.delta_api_postgres.modules.device.dto.request.CreateDeviceRequest;
import br.com.delta.delta_api_postgres.modules.device.dto.request.UpdateDeviceRequest;
import br.com.delta.delta_api_postgres.modules.device.mapper.DeviceMapper;
import br.com.delta.delta_api_postgres.modules.device.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delta/device")
@RequiredArgsConstructor
public class    DeviceController {

    private final DeviceService deviceService;
    private final DeviceMapper deviceMapper;

    @PostMapping
    public ResponseEntity<DeviceIO> create(
            @RequestBody @Valid CreateDeviceRequest request) {

        DeviceIO response = deviceService.create(
                deviceMapper.fromCreateRequest(request)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<DeviceIO>> findAll() {

        return ResponseEntity.ok(
                deviceService.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceIO> findById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                deviceService.findById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceIO> update(
            @PathVariable Integer id,
            @RequestBody @Valid UpdateDeviceRequest request) {

        DeviceIO response = deviceService.update(
                id,
                deviceMapper.fromUpdateRequest(id, request)
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id) {

        deviceService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
