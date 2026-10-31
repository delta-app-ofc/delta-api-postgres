package br.com.delta.delta_api_postgres;

import br.com.delta.delta_api_postgres.modules.address.entity.Address;
import br.com.delta.delta_api_postgres.modules.auth.entity.AuthUser;
import br.com.delta.delta_api_postgres.modules.auth.service.TokenService;
import br.com.delta.delta_api_postgres.modules.device.entity.Device;
import br.com.delta.delta_api_postgres.modules.habit.entity.Habit;
import br.com.delta.delta_api_postgres.modules.habit.entity.UserHabit;
import br.com.delta.delta_api_postgres.modules.habit.entity.DayOfWeek;
import br.com.delta.delta_api_postgres.modules.last_water_bill.entity.LastWaterBill;
import br.com.delta.delta_api_postgres.modules.organization.entity.Organization;
import br.com.delta.delta_api_postgres.modules.organization.enums.OrganizationSegment;
import br.com.delta.delta_api_postgres.modules.property.entity.Property;
import br.com.delta.delta_api_postgres.modules.property.entity.PropertyClassificationEntity;
import br.com.delta.delta_api_postgres.modules.property.enums.*;
import br.com.delta.delta_api_postgres.modules.region.entity.Region;
import br.com.delta.delta_api_postgres.modules.region.entity.Municipality;
import br.com.delta.delta_api_postgres.modules.region.enums.RegionName;
import br.com.delta.delta_api_postgres.modules.region.repository.MunicipalityRepository;
import br.com.delta.delta_api_postgres.modules.user_property.entity.UserProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserDataIntegrationTests extends AuthTestConfig {
    @Autowired MockMvc mvc;
    @Autowired EntityManager em;
    @Autowired TokenService tokens;
    @Autowired PasswordEncoder encoder;
    @Autowired Clock clock;
    @Autowired ObjectMapper json;
    @Autowired MunicipalityRepository municipalities;
    @MockitoBean StringRedisTemplate redis;

    AuthUser alice, bob;
    Property aliceProperty, bobProperty;
    Address aliceAddress, bobAddress;
    Device aliceDevice, bobDevice;
    LastWaterBill aliceBill, bobBill;
    UserProperty aliceLink, bobLink;
    UserHabit aliceHabit, bobHabit;
    DayOfWeek day;
    String bearer;

    @BeforeEach void setup() {
        @SuppressWarnings("unchecked") ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        alice = user("Alice");
        bob = user("Bob");
        Region region = em.createQuery("from Region", Region.class).setMaxResults(1).getResultStream()
                .findFirst().orElseGet(() -> {
                    var r = new Region();
                    ReflectionTestUtils.setField(r, "name", RegionName.GRANDE_SP);
                    em.persist(r);
                    return r;
                });
        municipalities.saveAndFlush(new Municipality("Santos", 3548500, region));
        var classification = em.createQuery("from PropertyClassificationEntity where name = :name", PropertyClassificationEntity.class)
                .setParameter("name", PropertyClassification.RESIDENCIAL_NORMAL).getResultStream().findFirst()
                .orElseGet(() -> {
                    var c = new PropertyClassificationEntity(null, PropertyClassification.RESIDENCIAL_NORMAL, PropertyClassificationGroup.RESIDENCIAL);
                    em.persist(c);
                    return c;
                });
        aliceAddress = address(region, "São Paulo");
        bobAddress = address(region, "Campinas");
        aliceProperty = property(aliceAddress, classification, "Casa Alice");
        bobProperty = property(bobAddress, classification, "Casa Bob");
        aliceLink = link(alice, aliceProperty);
        bobLink = link(bob, bobProperty);
        aliceDevice = device(aliceProperty);
        bobDevice = device(bobProperty);
        aliceBill = bill(alice);
        bobBill = bill(bob);
        var habit = new Habit(null, "Banho", "Higiene", "Banho diário");
        em.persist(habit);
        day = new DayOfWeek(null, "Segunda-feira");
        em.persist(day);
        aliceHabit = habit(alice, habit);
        bobHabit = habit(bob, habit);
        em.flush();
        bearer = "Bearer " + tokens.accessToken(alice, clock.instant());
    }

    @Test void profileIncludesOptionalFieldsWithoutSecrets() throws Exception {
        mvc.perform(get("/delta/auth/me").header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("id").value(alice.getId()))
                .andExpect(jsonPath("phone").value("11999999999"))
                .andExpect(jsonPath("birthDate").value("2000-01-01"))
                .andExpect(jsonPath("passwordHash").doesNotExist())
                .andExpect(jsonPath("enabled").doesNotExist());
    }

    @Test void updateProfileNormalizesAndKeepsTheSameIdentityAndPassword() throws Exception {
        String email = "updated-" + UUID.randomUUID() + "@example.com";
        String hash = alice.getPasswordHash();
        mvc.perform(put("/delta/auth/me").header("Authorization", bearer).contentType("application/json")
                        .content(profile(" Alice Nova ", " " + email.toUpperCase(Locale.ROOT) + " ", null)))
                .andExpect(status().isOk()).andExpect(jsonPath("name").value("Alice Nova"))
                .andExpect(jsonPath("email").value(email)).andExpect(jsonPath("phone").isEmpty());
        assertThat(alice.getPasswordHash()).isEqualTo(hash);
        assertThat(bob.getName()).isEqualTo("Bob");
        mvc.perform(get("/delta/auth/me").header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("email").value(email));
        mvc.perform(post("/delta/auth/login").contentType("application/json")
                        .content(json.writeValueAsString(Map.of("email", email, "password", "test-password-123"))))
                .andExpect(status().isOk());
    }

    @Test void profileRejectsDuplicateEmailAndInvalidFields() throws Exception {
        mvc.perform(put("/delta/auth/me").header("Authorization", bearer).contentType("application/json")
                        .content(profile("Alice", bob.getEmail(), null))).andExpect(status().isConflict());
        for (String invalid : List.of(profile("   ", alice.getEmail(), null),
                profile("Alice", "invalid", null), profile("Alice", alice.getEmail(), "1".repeat(16)),
                "{\"name\":\"Alice\",\"email\":\"alice@example.com\",\"birthDate\":\"2999-01-01\"}")) {
            mvc.perform(put("/delta/auth/me").header("Authorization", bearer).contentType("application/json")
                            .content(invalid)).andExpect(status().isBadRequest());
        }
    }

    @Test void profileDoesNotRequireCompletedRegistration() throws Exception {
        var newcomer = user("New");
        em.flush();
        mvc.perform(get("/delta/auth/me").header("Authorization", "Bearer " + tokens.accessToken(newcomer, clock.instant())))
                .andExpect(status().isOk()).andExpect(jsonPath("id").value(newcomer.getId()));
    }

    @Test void deletionDeactivatesAccountAndRejectsExistingJwtLoginAndRefresh() throws Exception {
        @SuppressWarnings("unchecked") ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(anyString())).thenReturn(alice.getId().toString());
        mvc.perform(delete("/delta/auth/me").header("Authorization", bearer)).andExpect(status().isNoContent());
        assertThat(alice.isEnabled()).isFalse();
        assertThat(em.find(Property.class, aliceProperty.getId())).isNotNull();
        assertThat(bob.isEnabled()).isTrue();
        mvc.perform(get("/delta/auth/me").header("Authorization", bearer)).andExpect(status().isUnauthorized());
        mvc.perform(post("/delta/auth/login").contentType("application/json")
                        .content(json.writeValueAsString(Map.of("email", alice.getEmail(), "password", "test-password-123"))))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/delta/auth/refresh").contentType("application/json").content("{\"refreshToken\":\"previous-token\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test void profileRequiresAuthenticationForAllMethods() throws Exception {
        mvc.perform(get("/delta/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(put("/delta/auth/me").contentType("application/json").content(profile("A", alice.getEmail(), null)))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/delta/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test void listsOnlyContainResourcesLinkedToAuthenticatedUser() throws Exception {
        for (String path : List.of("/delta/address", "/delta/property", "/delta/device", "/delta/last-water-bills",
                "/delta/habits/" + alice.getId(), "/delta/user-properties/" + alice.getId())) {
            mvc.perform(get(path).header("Authorization", bearer)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
        mvc.perform(get("/delta/property").header("Authorization", bearer))
                .andExpect(jsonPath("$[0].id").value(aliceProperty.getId()));
    }

    @Test void cannotReadOrDeleteOtherUsersResources() throws Exception {
        for (String path : List.of("/delta/address/" + bobAddress.getId(), "/delta/property/" + bobProperty.getId(),
                "/delta/device/" + bobDevice.getId(), "/delta/last-water-bills/" + bobBill.getId(),
                "/delta/habits/" + alice.getId() + "/" + bobHabit.getId(),
                "/delta/user-properties/" + alice.getId() + "/" + bobLink.getId())) {
            mvc.perform(get(path).header("Authorization", bearer)).andExpect(status().isNotFound());
            mvc.perform(delete(path).header("Authorization", bearer)).andExpect(status().isNotFound());
        }
    }

    @Test void cannotImpersonateUserInPathOrBody() throws Exception {
        for (String path : List.of("/delta/habits/" + bob.getId(), "/delta/user-properties/" + bob.getId(),
                "/delta/analytics/residential-efficiency-ranking/" + bob.getId())) {
            mvc.perform(get(path).header("Authorization", bearer)).andExpect(status().isForbidden());
        }
        mvc.perform(post("/delta/last-water-bills").header("Authorization", bearer).contentType("application/json")
                        .content(billBody(bob.getId()))).andExpect(status().isForbidden());
        mvc.perform(put("/delta/last-water-bills/" + aliceBill.getId()).header("Authorization", bearer)
                        .contentType("application/json").content(billBody(bob.getId()))).andExpect(status().isForbidden());
    }

    @Test void cannotUpdateOrAttachOtherUsersResources() throws Exception {
        mvc.perform(put("/delta/address/" + bobAddress.getId()).header("Authorization", bearer)
                .contentType("application/json").content(addressBody())).andExpect(status().isNotFound());
        mvc.perform(put("/delta/property/" + bobProperty.getId()).header("Authorization", bearer)
                .contentType("application/json").content(propertyBody(bobAddress.getId()))).andExpect(status().isNotFound());
        mvc.perform(put("/delta/property/" + aliceProperty.getId()).header("Authorization", bearer)
                .contentType("application/json").content(propertyBody(bobAddress.getId()))).andExpect(status().isNotFound());
        mvc.perform(put("/delta/device/" + aliceDevice.getId()).header("Authorization", bearer)
                .contentType("application/json").content(json.writeValueAsString(Map.of("deviceId", aliceDevice.getDeviceId(),
                        "propertyId", bobProperty.getId(), "isActive", true)))).andExpect(status().isNotFound());
        mvc.perform(post("/delta/user-properties/" + alice.getId()).header("Authorization", bearer)
                .contentType("application/json").content("{\"propertyId\":" + bobProperty.getId() + "}"))
                .andExpect(status().isNotFound());
    }

    @Test void ownAddressCanBeUpdatedButSharedAddressCannot() throws Exception {
        mvc.perform(put("/delta/address/" + aliceAddress.getId()).header("Authorization", bearer)
                        .contentType("application/json").content(addressBody()))
                .andExpect(status().isOk()).andExpect(jsonPath("city").value("Santos"));
        link(bob, aliceProperty);
        em.flush();
        mvc.perform(get("/delta/address/" + aliceAddress.getId()).header("Authorization", bearer)).andExpect(status().isOk());
        mvc.perform(put("/delta/address/" + aliceAddress.getId()).header("Authorization", bearer)
                .contentType("application/json").content(addressBody())).andExpect(status().isConflict());
        mvc.perform(delete("/delta/property/" + aliceProperty.getId()).header("Authorization", bearer))
                .andExpect(status().isConflict());
    }

    @Test void canRemoveOwnLinkWithoutDeletingSharedProperty() throws Exception {
        link(bob, aliceProperty);
        em.flush();
        mvc.perform(delete("/delta/user-properties/" + alice.getId() + "/" + aliceLink.getId())
                .header("Authorization", bearer)).andExpect(status().isNoContent());
        assertThat(em.find(Property.class, aliceProperty.getId())).isNotNull();
    }

    @Test void canDeleteOwnDeviceBillAndHabit() throws Exception {
        for (String path : List.of("/delta/device/" + aliceDevice.getId(), "/delta/last-water-bills/" + aliceBill.getId(),
                "/delta/habits/" + alice.getId() + "/" + aliceHabit.getId())) {
            mvc.perform(delete(path).header("Authorization", bearer)).andExpect(status().isNoContent());
            mvc.perform(get(path).header("Authorization", bearer)).andExpect(status().isNotFound());
        }
    }

    @Test void deviceDependencyBlocksPropertyDeletion() throws Exception {
        mvc.perform(delete("/delta/property/" + aliceProperty.getId()).header("Authorization", bearer))
                .andExpect(status().isConflict());
        assertThat(em.find(UserProperty.class, aliceLink.getId())).isNotNull();
    }

    @Test void referencedAddressCannotBeDeleted() throws Exception {
        mvc.perform(delete("/delta/address/" + aliceAddress.getId()).header("Authorization", bearer))
                .andExpect(status().isConflict());
        assertThat(em.find(Address.class, aliceAddress.getId())).isNotNull();
    }

    @Test void canUpdateOwnPropertyBillDeviceAndHabit() throws Exception {
        mvc.perform(put("/delta/property/" + aliceProperty.getId()).header("Authorization", bearer)
                .contentType("application/json").content(propertyBody(aliceAddress.getId()))).andExpect(status().isOk());
        mvc.perform(put("/delta/last-water-bills/" + aliceBill.getId()).header("Authorization", bearer)
                .contentType("application/json").content(billBody(alice.getId()))).andExpect(status().isOk());
        mvc.perform(put("/delta/device/" + aliceDevice.getId()).header("Authorization", bearer)
                .contentType("application/json").content(json.writeValueAsString(Map.of("deviceId", aliceDevice.getDeviceId(),
                        "propertyId", aliceProperty.getId(), "isActive", false)))).andExpect(status().isOk());
        mvc.perform(put("/delta/habits/" + alice.getId() + "/" + aliceHabit.getId()).header("Authorization", bearer)
                .contentType("application/json").content(json.writeValueAsString(Map.of("habitId", aliceHabit.getHabit().getId(),
                        "frequency", 3, "daysOfWeek", List.of(day.getId()))))).andExpect(status().isOk());
    }

    @Test void propertyWithoutDevicesCanBeDeletedWithItsOwnLink() throws Exception {
        em.remove(aliceDevice);
        em.flush();
        mvc.perform(delete("/delta/property/" + aliceProperty.getId()).header("Authorization", bearer))
                .andExpect(status().isNoContent());
        assertThat(em.find(Property.class, aliceProperty.getId())).isNull();
        assertThat(em.find(UserProperty.class, aliceLink.getId())).isNull();
        assertThat(em.find(Address.class, aliceAddress.getId())).isNotNull();
    }

    @Test void analyticsCannotBypassPropertyOwnership() throws Exception {
        for (String path : List.of("/delta/property/" + bobProperty.getId() + "/watercost?consumptionM3=10",
                "/delta/analytics/consumption-daily/" + bobProperty.getId(),
                "/delta/analytics/monthly-variation/" + bobProperty.getId())) {
            mvc.perform(get(path).header("Authorization", bearer)).andExpect(status().isNotFound());
        }
        mvc.perform(get("/delta/analytics/consumption-distribution").header("Authorization", bearer)).andExpect(status().isForbidden());
        mvc.perform(get("/delta/analytics/audit-history").header("Authorization", bearer)).andExpect(status().isForbidden());
    }

    @Test void publicCrudCannotAdoptAnAddressWithoutProvenOwnership() throws Exception {
        var unattached = address(aliceAddress.getRegion(), "Outro endereço");
        em.flush();
        mvc.perform(get("/delta/address/" + unattached.getId()).header("Authorization", bearer)).andExpect(status().isNotFound());
        var body = json.readTree(propertyBody(unattached.getId()));
        ((com.fasterxml.jackson.databind.node.ObjectNode) body).put("userId", alice.getId());
        mvc.perform(post("/delta/property").header("Authorization", bearer).contentType("application/json")
                .content(json.writeValueAsString(body))).andExpect(status().isNotFound());
        mvc.perform(put("/delta/property/" + aliceProperty.getId()).header("Authorization", bearer).contentType("application/json")
                .content(propertyBody(unattached.getId()))).andExpect(status().isNotFound());
    }

    @Test void habitsRejectNonPositiveFrequencyAndInvalidDays() throws Exception {
        for (Map<String, Object> body : List.<Map<String, Object>>of(
                Map.of("habitId", aliceHabit.getHabit().getId(), "frequency", 0, "daysOfWeek", List.of(day.getId())),
                Map.of("habitId", aliceHabit.getHabit().getId(), "frequency", 1, "daysOfWeek", List.of(-1)))) {
            mvc.perform(put("/delta/habits/" + alice.getId() + "/" + aliceHabit.getId()).header("Authorization", bearer)
                    .contentType("application/json").content(json.writeValueAsString(body))).andExpect(status().isBadRequest());
        }
    }

    @Test void organizationsHaveScopedReadsAndNoImplicitAdministration() throws Exception {
        var own = organization("11111111111111");
        var other = organization("22222222222222");
        aliceProperty.setOrganization(own);
        bobProperty.setOrganization(other);
        em.flush();
        mvc.perform(get("/delta/organizations").header("Authorization", bearer)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(own.getId()));
        mvc.perform(get("/delta/organizations/" + other.getId()).header("Authorization", bearer)).andExpect(status().isNotFound());
        String body = json.writeValueAsString(Map.of("corporateName", "Empresa", "tradeName", "Empresa",
                "cnpj", own.getCnpj(), "businessSegment", "VAREJO", "declaredUnitCount", 1));
        mvc.perform(post("/delta/organizations").header("Authorization", bearer).contentType("application/json").content(body))
                .andExpect(status().isForbidden());
        mvc.perform(put("/delta/organizations/" + own.getId()).header("Authorization", bearer).contentType("application/json").content(body))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/delta/organizations/" + own.getId()).header("Authorization", bearer)).andExpect(status().isForbidden());
    }

    private AuthUser user(String name) {
        var u = new AuthUser();
        u.setName(name); u.setEmail(UUID.randomUUID() + "@example.com");
        u.setBirthDate(LocalDate.of(2000, 1, 1)); u.setPhone("11999999999");
        u.setPasswordHash(encoder.encode("test-password-123"));
        em.persist(u); return u;
    }

    private Organization organization(String cnpj) {
        var o = new Organization(); o.setCorporateName("Empresa"); o.setTradeName("Empresa");
        o.setCnpj(cnpj); o.setBusinessSegment(OrganizationSegment.VAREJO);
        em.persist(o); return o;
    }

    private Address address(Region region, String city) {
        var a = new Address(null, region, "01001000", city, "SP", null, null);
        em.persist(a); return a;
    }

    private Property property(Address address, PropertyClassificationEntity classification, String name) {
        var p = new Property(); p.setName(name); p.setType(PropertyType.CASA);
        p.setAddress(address); p.setClassification(classification);
        em.persist(p); return p;
    }

    private UserProperty link(AuthUser user, Property property) {
        var link = new UserProperty(); link.setUserId(user.getId()); link.setProperty(property);
        em.persist(link); return link;
    }

    private Device device(Property property) {
        var d = new Device(); d.setDeviceId(UUID.randomUUID().toString()); d.setProperty(property);
        d.setInstallationDate(LocalDate.of(2026, 1, 1)); em.persist(d); return d;
    }

    private LastWaterBill bill(AuthUser user) {
        var b = new LastWaterBill(); b.setUserId(user.getId()); b.setMonth(LocalDate.of(2026, 1, 1));
        b.setTotalValue(BigDecimal.TEN); b.setM3Value(BigDecimal.ONE); em.persist(b); return b;
    }

    private UserHabit habit(AuthUser user, Habit habit) {
        var h = new UserHabit(); h.setUserId(user.getId()); h.setHabit(habit); h.setFrequency(2);
        em.persist(h); return h;
    }

    private String profile(String name, String email, String phone) throws Exception {
        var body = new HashMap<String, Object>();
        body.put("name", name); body.put("email", email); body.put("phone", phone); body.put("birthDate", "2000-01-01");
        return json.writeValueAsString(body);
    }

    private String addressBody() throws Exception {
        return json.writeValueAsString(Map.of("regionId", aliceAddress.getRegion().getId(), "cep", "11010000", "city", "Santos", "state", "SP"));
    }

    private String propertyBody(Integer addressId) throws Exception {
        return json.writeValueAsString(Map.of("name", "Casa", "type", "CASA", "classification", "RESIDENCIAL_NORMAL", "addressId", addressId));
    }

    private String billBody(Integer userId) throws Exception {
        return json.writeValueAsString(Map.of("userId", userId, "month", "2026-02-01", "totalValue", 10, "m3Value", 1));
    }
}
