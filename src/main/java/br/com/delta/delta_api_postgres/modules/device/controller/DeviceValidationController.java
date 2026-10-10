package br.com.delta.delta_api_postgres.modules.device.controller;

import br.com.delta.delta_api_postgres.modules.device.dto.io.DeviceValidationIO;
import br.com.delta.delta_api_postgres.modules.device.dto.request.ValidateDeviceRequest;
import br.com.delta.delta_api_postgres.modules.device.service.DeviceValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/delta/internal/devices")
@RequiredArgsConstructor
public class DeviceValidationController {
    private final DeviceValidationService service;

    @PostMapping("/validate")
    @io.swagger.v3.oas.annotations.Operation(summary = "Valida cadastro e situação ativa do dispositivo",
            description = "Consulta o device_id textual sem normalização. Dispositivo inexistente ou inativo retorna valid=false.",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "mongoServiceAuth"))
    public ResponseEntity<DeviceValidationIO> validate(@RequestBody @Valid ValidateDeviceRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.validate(request.deviceId()));
    }
}
