package br.com.delta.delta_api_postgres.modules.auth.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceAlreadyExistsException;
import br.com.delta.delta_api_postgres.modules.auth.dto.CompleteRegistrationRequest;
import br.com.delta.delta_api_postgres.modules.auth.repository.AuthUserRepository;
import br.com.delta.delta_api_postgres.modules.address.dto.io.AddressIO;
import br.com.delta.delta_api_postgres.modules.address.service.AddressService;
import br.com.delta.delta_api_postgres.modules.habit.dto.io.UserHabitIO;
import br.com.delta.delta_api_postgres.modules.habit.service.UserHabitService;
import br.com.delta.delta_api_postgres.modules.property.dto.io.PropertyIO;
import br.com.delta.delta_api_postgres.modules.property.service.PropertyService;
import br.com.delta.delta_api_postgres.modules.user_property.dto.io.UserPropertyIO;
import br.com.delta.delta_api_postgres.modules.user_property.service.UserPropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RegistrationCompletionService {
    private final AuthUserRepository users;
    private final AddressService addresses;
    private final PropertyService properties;
    private final UserPropertyService associations;
    private final UserHabitService habits;

    public record Result(Integer userId, boolean completed, AddressIO address,
                         PropertyIO property, List<UserHabitIO> habits) {}

    @Transactional(rollbackFor = Exception.class)
    public Result complete(Integer userId, CompleteRegistrationRequest request) {
        // Serialize completion/retries for this account, including concurrent mobile requests.
        users.lockById(userId).filter(user -> user.isEnabled())
                .orElseThrow(() -> new AccessDeniedException("Usuário inativo ou inexistente."));
        Set<Integer> habitIds = new HashSet<>();
        for (var habit : request.habits()) {
            if (!habitIds.add(habit.habitId()) || habit.frequency() <= 0
                    || new HashSet<>(habit.daysOfWeek()).size() != habit.daysOfWeek().size()
                    || habit.daysOfWeek().stream().anyMatch(day -> day == null || day <= 0))
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Informe hábitos únicos, frequência positiva e dias válidos sem repetição.");
        }

        var links = associations.findAll(userId);
        var existingHabits = habits.findAll(userId);
        if (!links.isEmpty()) {
            if (links.size() == 1) {
                var property = properties.findById(links.get(0).propertyId());
                var address = addresses.findById(property.addressId());
                if (matches(request, address, property, existingHabits))
                    return new Result(userId, true, address, property, existingHabits);
            }
            throw new ResourceAlreadyExistsException("Cadastro já possui dados diferentes. Utilize as rotas de atualização.");
        }
        if (!existingHabits.isEmpty())
            throw new ResourceAlreadyExistsException("Usuário já possui hábitos cadastrados. Utilize as rotas de atualização.");

        var a = request.address();
        var address = addresses.create(new AddressIO(null, a.regionId(), a.cep(), a.city(), a.state(),
                a.latitude(), a.longitude()));
        var p = request.property();
        var property = properties.create(userId, new PropertyIO(null, p.name(), p.type(),
                p.classification(), address.id(), p.organizationId(), p.builtAreaM2(), null));
        // The existing procedure may already create the association.
        if (associations.findAll(userId).stream().noneMatch(link -> link.propertyId().equals(property.id())))
            associations.create(new UserPropertyIO(null, userId, property.id(), null, null));
        var savedHabits = request.habits().stream().map(h -> habits.create(new UserHabitIO(
                null, userId, h.habitId(), null, null, h.frequency(), h.daysOfWeek()))).toList();
        return new Result(userId, true, address, property, savedHabits);
    }

    private boolean matches(CompleteRegistrationRequest request, AddressIO a, PropertyIO p,
                            List<UserHabitIO> savedHabits) {
        var address = request.address();
        var property = request.property();
        return Objects.equals(address.regionId(), a.regionId()) && Objects.equals(address.cep(), a.cep())
                && Objects.equals(address.city(), a.city()) && Objects.equals(address.state(), a.state())
                && Objects.equals(property.name(), p.name()) && property.type() == p.type()
                && property.classification() == p.classification()
                && Objects.equals(property.organizationId(), p.organizationId())
                && (property.builtAreaM2() == null ? p.builtAreaM2() == null
                    : p.builtAreaM2() != null && property.builtAreaM2().compareTo(p.builtAreaM2()) == 0)
                && request.habits().size() == savedHabits.size()
                && request.habits().stream().allMatch(h -> savedHabits.stream().anyMatch(saved ->
                    Objects.equals(h.habitId(), saved.habitId()) && Objects.equals(h.frequency(), saved.frequency())
                    && new HashSet<>(h.daysOfWeek()).equals(new HashSet<>(saved.daysOfWeek()))));
    }
}

