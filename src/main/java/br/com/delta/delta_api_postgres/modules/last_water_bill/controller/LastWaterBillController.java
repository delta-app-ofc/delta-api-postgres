package br.com.delta.delta_api_postgres.modules.last_water_bill.controller;

import br.com.delta.delta_api_postgres.modules.last_water_bill.dto.io.LastWaterBillIO;
import br.com.delta.delta_api_postgres.modules.last_water_bill.dto.request.CreateLastWaterBillRequest;
import br.com.delta.delta_api_postgres.modules.last_water_bill.dto.request.UpdateLastWaterBillRequest;
import br.com.delta.delta_api_postgres.modules.last_water_bill.mapper.LastWaterBillMapper;
import br.com.delta.delta_api_postgres.modules.last_water_bill.service.LastWaterBillService;
import br.com.delta.delta_api_postgres.modules.last_water_bill.swagger.LastWaterBillSwagger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delta/last-water-bills")
@RequiredArgsConstructor
public class LastWaterBillController implements LastWaterBillSwagger {

    private final LastWaterBillService lastWaterBillService;
    private final LastWaterBillMapper lastWaterBillMapper;

    @Override
    @PostMapping
    public ResponseEntity<LastWaterBillIO> create(
            @Valid @RequestBody CreateLastWaterBillRequest request
    ) {

        LastWaterBillIO io = lastWaterBillMapper.fromCreateRequest(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lastWaterBillService.create(io));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<LastWaterBillIO>> findAll() {

        return ResponseEntity.ok(
                lastWaterBillService.findAll()
        );
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<LastWaterBillIO> findById(
            @PathVariable Integer id
    ) {

        return ResponseEntity.ok(
                lastWaterBillService.findById(id)
        );
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<LastWaterBillIO> update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateLastWaterBillRequest request
    ) {

        LastWaterBillIO io = lastWaterBillMapper.fromUpdateRequest(id, request);

        return ResponseEntity.ok(
                lastWaterBillService.update(id, io)
        );
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {

        lastWaterBillService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
