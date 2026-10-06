package br.com.delta.delta_api_postgres.modules.device.controller;

import br.com.delta.delta_api_postgres.modules.device.dto.io.IssuedDeviceCredentialIO;
import br.com.delta.delta_api_postgres.modules.device.service.DeviceCredentialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/delta/device/{id}/credentials")
@RequiredArgsConstructor
public class DeviceCredentialController {
    private final DeviceCredentialService service;

    @PostMapping
    public ResponseEntity<IssuedDeviceCredentialIO> issue(@PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CacheControl.noStore()).body(service.issue(id));
    }
}
