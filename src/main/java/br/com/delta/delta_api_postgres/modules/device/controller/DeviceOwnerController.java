package br.com.delta.delta_api_postgres.modules.device.controller;

import br.com.delta.delta_api_postgres.modules.device.dto.io.DeviceOwnerIO;
import br.com.delta.delta_api_postgres.modules.device.service.DeviceOwnerService;
import br.com.delta.delta_api_postgres.modules.device.swagger.DeviceOwnerSwagger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/delta/internal/device")
@RequiredArgsConstructor
public class DeviceOwnerController implements DeviceOwnerSwagger {
    private final DeviceOwnerService deviceOwnerService;

    @Override
    @GetMapping("/{deviceId}/owner")
    public ResponseEntity<DeviceOwnerIO> findOwner(@PathVariable String deviceId) {
        return ResponseEntity.ok(deviceOwnerService.findByDeviceId(deviceId));
    }
}
