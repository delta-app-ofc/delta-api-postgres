package br.com.delta.delta_api_postgres;

import java.math.BigDecimal;
import br.com.delta.delta_api_postgres.modules.auth.dto.CompleteRegistrationRequest;
import br.com.delta.delta_api_postgres.modules.auth.entity.AuthUser;
import br.com.delta.delta_api_postgres.modules.auth.repository.AuthUserRepository;
import br.com.delta.delta_api_postgres.modules.auth.service.RegistrationCompletionService;
import br.com.delta.delta_api_postgres.modules.address.dto.request.CreateAddressRequest;
import br.com.delta.delta_api_postgres.modules.address.repository.AddressRepository;
import br.com.delta.delta_api_postgres.modules.property.service.PropertyService;
import br.com.delta.delta_api_postgres.modules.property.enums.*;
import br.com.delta.delta_api_postgres.modules.region.entity.Municipality;
import br.com.delta.delta_api_postgres.modules.region.entity.Region;
import br.com.delta.delta_api_postgres.modules.region.enums.RegionName;
import br.com.delta.delta_api_postgres.modules.region.repository.MunicipalityRepository;
import br.com.delta.delta_api_postgres.modules.region.repository.RegionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class RegistrationRollbackTests extends AuthTestConfig {
    @Autowired RegistrationCompletionService service;
    @Autowired AuthUserRepository users;
    @Autowired AddressRepository addresses;
    @Autowired RegionRepository regions;
    @Autowired MunicipalityRepository municipalities;
    @MockitoBean PropertyService properties;

    @org.junit.jupiter.api.AfterEach void clearSecurity() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test void propertyFailureRollsBackAddressButKeepsPreviouslyCreatedAccount() {
        var user = new AuthUser();
        user.setName("Rollback"); user.setEmail("rollback@example.com");
        user.setPasswordHash("test-hash"); user.setBirthDate(LocalDate.of(2000, 1, 1));
        user = users.saveAndFlush(user);
        var region = new Region();
        ReflectionTestUtils.setField(region, "name", RegionName.GRANDE_SP);
        region = regions.saveAndFlush(region);
        municipalities.saveAndFlush(new Municipality("São Paulo", 3550308, region));
        var request = new CompleteRegistrationRequest(
                new CreateAddressRequest("01001000", "São Paulo", "SP", new BigDecimal("-23.550520"), new BigDecimal("-46.633308")),
                new CompleteRegistrationRequest.PropertyDetails("Casa", PropertyType.CASA,
                        PropertyClassification.RESIDENCIAL_NORMAL, null, null), List.of());
        var before = addresses.count();
        Integer userId = user.getId();
        var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("test")
                .header("alg", "RS256").subject(userId.toString()).build();
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(jwt));
        when(properties.create(eq(userId), any())).thenAnswer(invocation -> {
            assertThat(addresses.count()).isEqualTo(before + 1);
            throw new IllegalStateException("Simulated property failure");
        });
        assertThatThrownBy(() -> service.complete(userId, request)).isInstanceOf(IllegalStateException.class);
        assertThat(addresses.count()).isEqualTo(before);
        assertThat(users.existsById(userId)).isTrue();
    }
}
