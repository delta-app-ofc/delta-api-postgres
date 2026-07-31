package br.com.delta.delta_api_postgres.controller;

import br.com.delta.delta_api_postgres.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/delta/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping()
    public ResponseEntity<DeviceResponseDTO> create(
            @RequestBody DeviceCreateDTO dto) {

        DeviceResponseDTO response = deviceService.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping()
    public ResponseEntity<List<DeviceResponseDTO>> findAll() {

        return ResponseEntity.ok(deviceService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponseDTO> findById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(deviceService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponseDTO> update(
            @PathVariable Integer id,
            @RequestBody DeviceUpdateDTO dto) {

        return ResponseEntity.ok(deviceService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id) {

        deviceService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
