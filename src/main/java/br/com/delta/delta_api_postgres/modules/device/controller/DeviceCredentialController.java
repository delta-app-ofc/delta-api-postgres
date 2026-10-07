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

    @PostMapping("/rotate")
    public ResponseEntity<IssuedDeviceCredentialIO> rotate(@PathVariable Integer id) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.rotate(id));
    }

    @DeleteMapping("/current")
    public ResponseEntity<Void> revoke(@PathVariable Integer id) {
        service.revoke(id);
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }
}
