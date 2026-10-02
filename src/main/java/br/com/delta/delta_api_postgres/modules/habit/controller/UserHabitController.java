package br.com.delta.delta_api_postgres.modules.habit.controller;

import br.com.delta.delta_api_postgres.modules.habit.dto.io.UserHabitIO;
import br.com.delta.delta_api_postgres.modules.habit.dto.requests.CreateUserHabitRequest;
import br.com.delta.delta_api_postgres.modules.habit.dto.requests.UpdateUserHabitRequest;
import br.com.delta.delta_api_postgres.modules.habit.mapper.UserHabitMapper;
import br.com.delta.delta_api_postgres.modules.habit.service.UserHabitService;
import br.com.delta.delta_api_postgres.modules.habit.swagger.UserHabitSwagger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delta/habits/{userId}")
@RequiredArgsConstructor
public class UserHabitController implements UserHabitSwagger {

    private final UserHabitService userHabitService;
    private final UserHabitMapper userHabitMapper;

    @Override
    @PostMapping
    public ResponseEntity<UserHabitIO> create(
            @PathVariable Integer userId,
            @Valid @RequestBody CreateUserHabitRequest request
    ) {

        UserHabitIO io = userHabitMapper.fromCreateRequest(
                userId,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userHabitService.create(io));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<UserHabitIO>> findAll(
            @PathVariable Integer userId
    ) {

        return ResponseEntity.ok(
                userHabitService.findAll(userId)
        );
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<UserHabitIO> findById(
            @PathVariable Integer userId,
            @PathVariable Integer id
    ) {

        return ResponseEntity.ok(
                userHabitService.findById(userId, id)
        );
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<UserHabitIO> update(
            @PathVariable Integer userId,
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserHabitRequest request
    ) {

        UserHabitIO io = userHabitMapper.fromUpdateRequest(
                id,
                userId,
                request
        );

        return ResponseEntity.ok(
                userHabitService.update(userId, id, io)
        );
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer userId,
            @PathVariable Integer id
    ) {

        userHabitService.delete(userId, id);

        return ResponseEntity.noContent().build();
    }
}
