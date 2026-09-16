package br.com.delta.delta_api_postgres.modules.user_property.controller;

import br.com.delta.delta_api_postgres.modules.user_property.dto.io.UserPropertyIO;
import br.com.delta.delta_api_postgres.modules.user_property.dto.request.CreateUserPropertyRequest;
import br.com.delta.delta_api_postgres.modules.user_property.dto.request.UpdateUserPropertyRequest;
import br.com.delta.delta_api_postgres.modules.user_property.mapper.UserPropertyMapper;
import br.com.delta.delta_api_postgres.modules.user_property.service.UserPropertyService;
import br.com.delta.delta_api_postgres.modules.user_property.swagger.UserPropertySwagger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delta/user-properties/{userId}")
@RequiredArgsConstructor
public class UserPropertyController implements UserPropertySwagger {

    private final UserPropertyService userPropertyService;
    private final UserPropertyMapper userPropertyMapper;

    @Override
    @PostMapping
    public ResponseEntity<UserPropertyIO> create(
            @PathVariable Integer userId,
            @Valid @RequestBody CreateUserPropertyRequest request
    ) {

        UserPropertyIO io = userPropertyMapper.fromCreateRequest(
                userId,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userPropertyService.create(io));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<UserPropertyIO>> findAll(
            @PathVariable Integer userId
    ) {

        return ResponseEntity.ok(
                userPropertyService.findAll(userId)
        );
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<UserPropertyIO> findById(
            @PathVariable Integer userId,
            @PathVariable Integer id
    ) {

        return ResponseEntity.ok(
                userPropertyService.findById(userId, id)
        );
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<UserPropertyIO> update(
            @PathVariable Integer userId,
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserPropertyRequest request
    ) {

        UserPropertyIO io = userPropertyMapper.fromUpdateRequest(
                id,
                userId,
                request
        );

        return ResponseEntity.ok(
                userPropertyService.update(userId, id, io)
        );
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer userId,
            @PathVariable Integer id
    ) {

        userPropertyService.delete(userId, id);

        return ResponseEntity.noContent().build();
    }
}
