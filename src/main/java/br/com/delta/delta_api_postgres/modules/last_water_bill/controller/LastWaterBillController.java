package br.com.delta.delta_api_postgres.modules.last_water_bill.controller;

import br.com.delta.delta_api_postgres.modules.last_water_bill.dto.io.LastWaterBillIO;
import br.com.delta.delta_api_postgres.modules.last_water_bill.dto.request.CreateLastWaterBillRequest;
import br.com.delta.delta_api_postgres.modules.last_water_bill.dto.request.UpdateLastWaterBillRequest;
import br.com.delta.delta_api_postgres.modules.last_water_bill.mapper.LastWaterBillMapper;
import br.com.delta.delta_api_postgres.modules.last_water_bill.service.LastWaterBillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delta/last-water-bills")
@RequiredArgsConstructor
public class LastWaterBillController {

    private final LastWaterBillService lastWaterBillService;
    private final LastWaterBillMapper lastWaterBillMapper;

    @PostMapping
    public ResponseEntity<LastWaterBillIO> create(
            @Valid @RequestBody CreateLastWaterBillRequest request
    ) {

        LastWaterBillIO io = lastWaterBillMapper.fromCreateRequest(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lastWaterBillService.create(io));
    }

    @GetMapping
    public ResponseEntity<List<LastWaterBillIO>> findAll() {

        return ResponseEntity.ok(
                lastWaterBillService.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<LastWaterBillIO> findById(
            @PathVariable Integer id
    ) {

        return ResponseEntity.ok(
                lastWaterBillService.findById(id)
        );
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {

        lastWaterBillService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
