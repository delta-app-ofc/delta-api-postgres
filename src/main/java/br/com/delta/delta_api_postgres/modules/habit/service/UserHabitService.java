package br.com.delta.delta_api_postgres.modules.habit.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.common.exception.ResourceAlreadyExistsException;
import br.com.delta.delta_api_postgres.modules.habit.dto.io.UserHabitIO;
import br.com.delta.delta_api_postgres.modules.habit.entity.DayOfWeek;
import br.com.delta.delta_api_postgres.modules.habit.entity.Habit;
import br.com.delta.delta_api_postgres.modules.habit.entity.UserHabit;
import br.com.delta.delta_api_postgres.modules.habit.entity.UserHabitDay;
import br.com.delta.delta_api_postgres.modules.habit.mapper.UserHabitMapper;
import br.com.delta.delta_api_postgres.modules.habit.repository.DayOfWeekRepository;
import br.com.delta.delta_api_postgres.modules.habit.repository.HabitRepository;
import br.com.delta.delta_api_postgres.modules.habit.repository.UserHabitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserHabitService {

    private final UserHabitRepository userHabitRepository;
    private final HabitRepository habitRepository;
    private final DayOfWeekRepository dayOfWeekRepository;
    private final UserHabitMapper userHabitMapper;

    public UserHabitIO create(UserHabitIO io) {

        validateTimes(io);

        Habit habit = habitRepository.findById(io.habitId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Hábito não encontrado"
                        )
                );

        UserHabit userHabit = new UserHabit();

        userHabit.setUserId(io.userId());
        userHabit.setHabit(habit);
        userHabit.setFrequency(io.frequency());
        userHabit.setStartTime(io.startTime());
        userHabit.setEndTime(io.endTime());
        userHabit.setEstimatedLiters(io.estimatedLiters());

        addDays(userHabit, io.daysOfWeek());

        UserHabit saved = userHabitRepository.save(userHabit);

        return userHabitMapper.toIO(saved);
    }

    @Transactional(readOnly = true)
    public List<UserHabitIO> findAll(Integer userId) {

        return userHabitRepository.findByUserId(userId)
                .stream()
                .map(userHabitMapper::toIO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserHabitIO findById(
            Integer userId,
            Integer id
    ) {

        UserHabit userHabit = userHabitRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Hábito do usuário não encontrado"
                        )
                );

        validateOwnership(userHabit, userId);

        return userHabitMapper.toIO(userHabit);
    }

    public UserHabitIO update(
            Integer userId,
            Integer id,
            UserHabitIO io
    ) {

        validateTimes(io);

        UserHabit userHabit = userHabitRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Hábito do usuário não encontrado"
                        )
                );

        validateOwnership(userHabit, userId);

        if (!userHabit.getHabit().getId().equals(io.habitId()) &&
                userHabitRepository.existsByUserIdAndHabit_Id(
                        userId,
                        io.habitId()
                )) {
            throw new ResourceAlreadyExistsException(
                    "O usuário já possui o novo hábito informado"
            );
        }

        Habit habit = habitRepository.findById(io.habitId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Novo hábito não encontrado"
                        )
                );

        userHabit.setHabit(habit);
        userHabit.setFrequency(io.frequency());
        userHabit.setStartTime(io.startTime());
        userHabit.setEndTime(io.endTime());
        userHabit.setEstimatedLiters(io.estimatedLiters());

        syncDays(userHabit, io.daysOfWeek());

        return userHabitMapper.toIO(userHabit);
    }

    public void delete(
            Integer userId,
            Integer id
    ) {

        UserHabit userHabit = userHabitRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Hábito do usuário não encontrado"
                        )
                );

        validateOwnership(userHabit, userId);

        userHabitRepository.delete(userHabit);
    }

    // Espelha chk_tb_user_habit_time do banco: devolve 400 em vez de erro 500.
    private void validateTimes(UserHabitIO io) {

        boolean onlyOne = (io.startTime() == null) != (io.endTime() == null);
        boolean inverted = io.startTime() != null && io.endTime() != null
                && !io.endTime().isAfter(io.startTime());

        if (onlyOne || inverted) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe horário de início e fim juntos, com o fim depois do início"
            );
        }
    }

    private void validateOwnership(
            UserHabit userHabit,
            Integer userId
    ) {

        if (!userHabit.getUserId().equals(userId)) {
            throw new ResourceNotFoundException(
                    "Hábito do usuário não encontrado"
            );
        }
    }

    private void addDays(
            UserHabit userHabit,
            Collection<Integer> daysOfWeek
    ) {

        for (Integer dayId : new LinkedHashSet<>(daysOfWeek)) {

            DayOfWeek day = dayOfWeekRepository
                    .findById(dayId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Dia da semana não encontrado"
                            )
                    );

            UserHabitDay userHabitDay = new UserHabitDay();

            userHabitDay.setUserHabit(userHabit);
            userHabitDay.setDayOfWeek(day);

            userHabit.getDays().add(userHabitDay);
        }
    }

    private void syncDays(
            UserHabit userHabit,
            List<Integer> daysOfWeek
    ) {

        Set<Integer> requestedDayIds = new LinkedHashSet<>(daysOfWeek);

        userHabit.getDays().removeIf(userHabitDay ->
                !requestedDayIds.contains(
                        userHabitDay.getDayOfWeek().getId()
                )
        );

        Set<Integer> currentDayIds = new HashSet<>();

        for (UserHabitDay userHabitDay : userHabit.getDays()) {
            currentDayIds.add(userHabitDay.getDayOfWeek().getId());
        }

        requestedDayIds.removeAll(currentDayIds);
        addDays(userHabit, requestedDayIds);
    }
}
