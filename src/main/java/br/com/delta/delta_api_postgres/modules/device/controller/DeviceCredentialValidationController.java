package br.com.delta.delta_api_postgres.modules.device.controller;

import br.com.delta.delta_api_postgres.modules.device.dto.io.DeviceCredentialValidationIO;
import br.com.delta.delta_api_postgres.modules.device.dto.request.ValidateDeviceCredentialRequest;
import br.com.delta.delta_api_postgres.modules.device.service.DeviceCredentialValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/delta/internal/device-auth")
@RequiredArgsConstructor
public class DeviceCredentialValidationController {
    private final DeviceCredentialValidationService service;

    @PostMapping("/validate")
    @io.swagger.v3.oas.annotations.Operation(security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "mongoServiceAuth"))
    public ResponseEntity<DeviceCredentialValidationIO> validate(@RequestBody @Valid ValidateDeviceCredentialRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.validate(request.apiKey()));
    }
}
