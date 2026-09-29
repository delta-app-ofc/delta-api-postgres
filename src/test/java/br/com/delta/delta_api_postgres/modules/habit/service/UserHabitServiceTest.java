package br.com.delta.delta_api_postgres.modules.habit.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceAlreadyExistsException;
import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.habit.dto.io.UserHabitIO;
import br.com.delta.delta_api_postgres.modules.habit.entity.DayOfWeek;
import br.com.delta.delta_api_postgres.modules.habit.entity.Habit;
import br.com.delta.delta_api_postgres.modules.habit.entity.UserHabit;
import br.com.delta.delta_api_postgres.modules.habit.entity.UserHabitDay;
import br.com.delta.delta_api_postgres.modules.habit.mapper.UserHabitMapper;
import br.com.delta.delta_api_postgres.modules.habit.repository.DayOfWeekRepository;
import br.com.delta.delta_api_postgres.modules.habit.repository.HabitRepository;
import br.com.delta.delta_api_postgres.modules.habit.repository.UserHabitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserHabitServiceTest {

    private static final Integer USER_HABIT_ID = 1;
    private static final Integer USER_ID = 10;
    private static final Integer OTHER_USER_ID = 99;
    private static final Integer HABIT_ID = 100;
    private static final Integer NEW_HABIT_ID = 200;
    private static final Integer FREQUENCY = 3;

    @Mock
    private UserHabitRepository userHabitRepository;

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private DayOfWeekRepository dayOfWeekRepository;

    @Mock
    private UserHabitMapper userHabitMapper;

    @InjectMocks
    private UserHabitService userHabitService;

    @Test
    void create_quandoDadosValidos_deveSalvarERetornarHabitoDoUsuario() {
        // Arrange
        UserHabitIO input = inputUserHabitIO(HABIT_ID, List.of(1, 2, 1));
        UserHabitIO expected = savedUserHabitIO(HABIT_ID, List.of(1, 2));
        Habit habit = habit(HABIT_ID);
        DayOfWeek monday = day(1, "Segunda-feira");
        DayOfWeek tuesday = day(2, "Terça-feira");

        when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habit));
        when(dayOfWeekRepository.findById(1)).thenReturn(Optional.of(monday));
        when(dayOfWeekRepository.findById(2)).thenReturn(Optional.of(tuesday));
        when(userHabitRepository.save(any(UserHabit.class))).thenAnswer(invocation -> {
            UserHabit saved = invocation.getArgument(0);
            saved.setId(USER_HABIT_ID);
            return saved;
        });
        when(userHabitMapper.toIO(any(UserHabit.class))).thenReturn(expected);

        // Act
        UserHabitIO result = userHabitService.create(input);

        // Assert
        assertThat(result).isEqualTo(expected);

        ArgumentCaptor<UserHabit> captor = ArgumentCaptor.forClass(UserHabit.class);
        verify(userHabitRepository).save(captor.capture());

        UserHabit saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getHabit()).isSameAs(habit);
        assertThat(saved.getFrequency()).isEqualTo(FREQUENCY);
        assertThat(dayIds(saved)).containsExactly(1, 2);
        assertThat(saved.getDays())
                .allSatisfy(userHabitDay -> assertThat(userHabitDay.getUserHabit()).isSameAs(saved));

        verify(dayOfWeekRepository, times(1)).findById(1);
        verify(dayOfWeekRepository, times(1)).findById(2);
        verify(userHabitMapper).toIO(saved);
    }

    @Test
    void create_quandoHabitoNaoExiste_deveLancarResourceNotFoundException() {
        UserHabitIO input = inputUserHabitIO(HABIT_ID, List.of(1));
        when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userHabitService.create(input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hábito não encontrado");

        verify(userHabitRepository, never()).save(any());
        verifyNoInteractions(dayOfWeekRepository, userHabitMapper);
    }

    @Test
    void create_quandoDiaDaSemanaNaoExiste_deveLancarResourceNotFoundException() {
        UserHabitIO input = inputUserHabitIO(HABIT_ID, List.of(1, 99));

        when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habit(HABIT_ID)));
        when(dayOfWeekRepository.findById(1)).thenReturn(Optional.of(day(1, "Segunda-feira")));
        when(dayOfWeekRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userHabitService.create(input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Dia da semana não encontrado");

        verify(userHabitRepository, never()).save(any());
        verifyNoInteractions(userHabitMapper);
    }

    @Test
    void findAll_quandoExistemHabitos_deveRetornarTodosDoUsuarioMapeados() {
        UserHabit first = userHabit(USER_HABIT_ID, USER_ID, habit(HABIT_ID), FREQUENCY, List.of(1));
        UserHabit second = userHabit(2, USER_ID, habit(NEW_HABIT_ID), 5, List.of(2));
        UserHabitIO firstIO = savedUserHabitIO(HABIT_ID, List.of(1));
        UserHabitIO secondIO = new UserHabitIO(2, USER_ID, NEW_HABIT_ID,
                "Novo hábito", "Descrição", 5, List.of(2));

        when(userHabitRepository.findByUserId(USER_ID)).thenReturn(List.of(first, second));
        when(userHabitMapper.toIO(first)).thenReturn(firstIO);
        when(userHabitMapper.toIO(second)).thenReturn(secondIO);

        List<UserHabitIO> result = userHabitService.findAll(USER_ID);

        assertThat(result).containsExactly(firstIO, secondIO);
        verify(userHabitMapper).toIO(first);
        verify(userHabitMapper).toIO(second);
    }

    @Test
    void findAll_quandoNaoExistemHabitos_deveRetornarListaVazia() {
        when(userHabitRepository.findByUserId(USER_ID)).thenReturn(List.of());

        List<UserHabitIO> result = userHabitService.findAll(USER_ID);

        assertThat(result).isEmpty();
        verifyNoInteractions(userHabitMapper);
    }

    @Test
    void findById_quandoHabitoExisteEPertenceAoUsuario_deveRetornarHabitoMapeado() {
        UserHabit userHabit = userHabit(USER_HABIT_ID, USER_ID, habit(HABIT_ID), FREQUENCY, List.of(1, 2));
        UserHabitIO expected = savedUserHabitIO(HABIT_ID, List.of(1, 2));

        when(userHabitRepository.findById(USER_HABIT_ID)).thenReturn(Optional.of(userHabit));
        when(userHabitMapper.toIO(userHabit)).thenReturn(expected);

        UserHabitIO result = userHabitService.findById(USER_ID, USER_HABIT_ID);

        assertThat(result).isEqualTo(expected);
        verify(userHabitMapper).toIO(userHabit);
    }

    @Test
    void findById_quandoHabitoNaoExiste_deveLancarResourceNotFoundException() {
        when(userHabitRepository.findById(USER_HABIT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userHabitService.findById(USER_ID, USER_HABIT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hábito do usuário não encontrado");

        verifyNoInteractions(userHabitMapper);
    }

    @Test
    void findById_quandoHabitoPertenceAOutroUsuario_deveLancarResourceNotFoundException() {
        UserHabit userHabit = userHabit(USER_HABIT_ID, OTHER_USER_ID, habit(HABIT_ID), FREQUENCY, List.of(1));
        when(userHabitRepository.findById(USER_HABIT_ID)).thenReturn(Optional.of(userHabit));

        assertThatThrownBy(() -> userHabitService.findById(USER_ID, USER_HABIT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hábito do usuário não encontrado");

        verifyNoInteractions(userHabitMapper);
    }

    @Test
    void update_quandoDadosValidos_deveAtualizarHabitoFrequenciaEDias() {
        Habit currentHabit = habit(HABIT_ID);
        Habit newHabit = habit(NEW_HABIT_ID);
        UserHabit userHabit = userHabit(
                USER_HABIT_ID,
                USER_ID,
                currentHabit,
                FREQUENCY,
                List.of(1, 2)
        );
        UserHabitIO input = new UserHabitIO(
                USER_HABIT_ID,
                USER_ID,
                NEW_HABIT_ID,
                null,
                null,
                5,
                List.of(2, 3, 3)
        );
        UserHabitIO expected = new UserHabitIO(
                USER_HABIT_ID,
                USER_ID,
                NEW_HABIT_ID,
                "Novo hábito",
                "Descrição",
                5,
                List.of(2, 3)
        );

        when(userHabitRepository.findById(USER_HABIT_ID)).thenReturn(Optional.of(userHabit));
        when(userHabitRepository.existsByUserIdAndHabit_Id(USER_ID, NEW_HABIT_ID)).thenReturn(false);
        when(habitRepository.findById(NEW_HABIT_ID)).thenReturn(Optional.of(newHabit));
        when(dayOfWeekRepository.findById(3)).thenReturn(Optional.of(day(3, "Quarta-feira")));
        when(userHabitMapper.toIO(userHabit)).thenReturn(expected);

        UserHabitIO result = userHabitService.update(USER_ID, USER_HABIT_ID, input);

        assertThat(result).isEqualTo(expected);
        assertThat(userHabit.getHabit()).isSameAs(newHabit);
        assertThat(userHabit.getFrequency()).isEqualTo(5);
        assertThat(dayIds(userHabit)).containsExactly(2, 3);
        assertThat(userHabit.getDays().get(1).getUserHabit()).isSameAs(userHabit);
        verify(dayOfWeekRepository).findById(3);
        verify(userHabitRepository, never()).save(any());
    }

    @Test
    void update_quandoHabitoDoUsuarioNaoExiste_deveLancarResourceNotFoundException() {
        UserHabitIO input = inputUserHabitIO(NEW_HABIT_ID, List.of(1));
        when(userHabitRepository.findById(USER_HABIT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userHabitService.update(USER_ID, USER_HABIT_ID, input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hábito do usuário não encontrado");

        verifyNoInteractions(habitRepository, dayOfWeekRepository, userHabitMapper);
    }

    @Test
    void update_quandoHabitoPertenceAOutroUsuario_deveLancarResourceNotFoundException() {
        UserHabit userHabit = userHabit(USER_HABIT_ID, OTHER_USER_ID, habit(HABIT_ID), FREQUENCY, List.of(1));
        UserHabitIO input = inputUserHabitIO(NEW_HABIT_ID, List.of(1));
        when(userHabitRepository.findById(USER_HABIT_ID)).thenReturn(Optional.of(userHabit));

        assertThatThrownBy(() -> userHabitService.update(USER_ID, USER_HABIT_ID, input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hábito do usuário não encontrado");

        verifyNoInteractions(habitRepository, dayOfWeekRepository, userHabitMapper);
    }

    @Test
    void update_quandoUsuarioJaPossuiNovoHabito_deveLancarResourceAlreadyExistsException() {
        UserHabit userHabit = userHabit(USER_HABIT_ID, USER_ID, habit(HABIT_ID), FREQUENCY, List.of(1));
        UserHabitIO input = inputUserHabitIO(NEW_HABIT_ID, List.of(1));

        when(userHabitRepository.findById(USER_HABIT_ID)).thenReturn(Optional.of(userHabit));
        when(userHabitRepository.existsByUserIdAndHabit_Id(USER_ID, NEW_HABIT_ID)).thenReturn(true);

        assertThatThrownBy(() -> userHabitService.update(USER_ID, USER_HABIT_ID, input))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("O usuário já possui o novo hábito informado");

        verifyNoInteractions(habitRepository, dayOfWeekRepository, userHabitMapper);
    }

    @Test
    void update_quandoNovoHabitoNaoExiste_deveLancarResourceNotFoundException() {
        UserHabit userHabit = userHabit(USER_HABIT_ID, USER_ID, habit(HABIT_ID), FREQUENCY, List.of(1));
        UserHabitIO input = inputUserHabitIO(NEW_HABIT_ID, List.of(1));

        when(userHabitRepository.findById(USER_HABIT_ID)).thenReturn(Optional.of(userHabit));
        when(userHabitRepository.existsByUserIdAndHabit_Id(USER_ID, NEW_HABIT_ID)).thenReturn(false);
        when(habitRepository.findById(NEW_HABIT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userHabitService.update(USER_ID, USER_HABIT_ID, input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Novo hábito não encontrado");

        verifyNoInteractions(dayOfWeekRepository, userHabitMapper);
    }

    @Test
    void update_quandoNovoDiaNaoExiste_deveLancarResourceNotFoundException() {
        UserHabit userHabit = userHabit(USER_HABIT_ID, USER_ID, habit(HABIT_ID), FREQUENCY, List.of());
        UserHabitIO input = inputUserHabitIO(NEW_HABIT_ID, List.of(99));

        when(userHabitRepository.findById(USER_HABIT_ID)).thenReturn(Optional.of(userHabit));
        when(userHabitRepository.existsByUserIdAndHabit_Id(USER_ID, NEW_HABIT_ID)).thenReturn(false);
        when(habitRepository.findById(NEW_HABIT_ID)).thenReturn(Optional.of(habit(NEW_HABIT_ID)));
        when(dayOfWeekRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userHabitService.update(USER_ID, USER_HABIT_ID, input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Dia da semana não encontrado");

        verifyNoInteractions(userHabitMapper);
    }

    @Test
    void delete_quandoHabitoExisteEPertenceAoUsuario_deveExcluirHabito() {
        UserHabit userHabit = userHabit(USER_HABIT_ID, USER_ID, habit(HABIT_ID), FREQUENCY, List.of(1));
        when(userHabitRepository.findById(USER_HABIT_ID)).thenReturn(Optional.of(userHabit));

        userHabitService.delete(USER_ID, USER_HABIT_ID);

        verify(userHabitRepository).delete(userHabit);
    }

    @Test
    void delete_quandoHabitoNaoExiste_deveLancarResourceNotFoundException() {
        when(userHabitRepository.findById(USER_HABIT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userHabitService.delete(USER_ID, USER_HABIT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hábito do usuário não encontrado");

        verify(userHabitRepository, never()).delete(any());
    }

    @Test
    void delete_quandoHabitoPertenceAOutroUsuario_deveLancarResourceNotFoundException() {
        UserHabit userHabit = userHabit(USER_HABIT_ID, OTHER_USER_ID, habit(HABIT_ID), FREQUENCY, List.of(1));
        when(userHabitRepository.findById(USER_HABIT_ID)).thenReturn(Optional.of(userHabit));

        assertThatThrownBy(() -> userHabitService.delete(USER_ID, USER_HABIT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hábito do usuário não encontrado");

        verify(userHabitRepository, never()).delete(any());
    }

    private UserHabitIO inputUserHabitIO(Integer habitId, List<Integer> days) {
        return new UserHabitIO(null, USER_ID, habitId, null, null, FREQUENCY, days);
    }

    private UserHabitIO savedUserHabitIO(Integer habitId, List<Integer> days) {
        return new UserHabitIO(
                USER_HABIT_ID,
                USER_ID,
                habitId,
                "Hábito",
                "Descrição",
                FREQUENCY,
                days
        );
    }

    private Habit habit(Integer id) {
        return new Habit(id, "Hábito", "Descrição");
    }

    private DayOfWeek day(Integer id, String name) {
        return new DayOfWeek(id, name);
    }

    private UserHabit userHabit(
            Integer id,
            Integer userId,
            Habit habit,
            Integer frequency,
            List<Integer> dayIds
    ) {
        UserHabit userHabit = new UserHabit(id, userId, habit, frequency, new ArrayList<>());

        dayIds.forEach(dayId -> userHabit.getDays().add(
                new UserHabitDay(null, userHabit, day(dayId, "Dia " + dayId))
        ));

        return userHabit;
    }

    private List<Integer> dayIds(UserHabit userHabit) {
        return userHabit.getDays()
                .stream()
                .map(userHabitDay -> userHabitDay.getDayOfWeek().getId())
                .toList();
    }
}
