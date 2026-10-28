package br.com.delta.delta_api_postgres;

import br.com.delta.delta_api_postgres.common.service.HealthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DeltaApiPostgresApplicationTests extends AuthTestConfig {

	@Autowired MockMvc mvc;
	@Autowired @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mappings;
	@MockitoBean HealthService healthService;

	@Test
	void contextLoads() {
	}

	@Test
	void healthIsAvailableWithoutAuthentication() throws Exception {
		when(healthService.isHealthy()).thenReturn(true);
		mvc.perform(get("/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
	}

	@Test
	void healthReturnsServiceUnavailableWhenDependencyIsDown() throws Exception {
		when(healthService.isHealthy()).thenReturn(false);
		mvc.perform(get("/health"))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.status").value("DOWN"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"/delta/internal/devices/validate", "/delta/internal/device-auth/validate"})
	void removedValidationRoutesRemainProtected(String path) throws Exception {
		mvc.perform(post(path).contentType("application/json").content("{}"))
				.andExpect(status().isUnauthorized());
	}

	@ParameterizedTest
	@ValueSource(strings = {"/delta/internal/devices/validate", "/delta/internal/device-auth/validate"})
	void removedValidationRoutesHaveNoHandler(String path) throws Exception {
		assertThat(mappings.getHandler(new MockHttpServletRequest("POST", path))).isNull();
	}

	@Test
	void openApiDoesNotAdvertiseSqlValidation() throws Exception {
		mvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paths['/delta/internal/devices/validate']").doesNotExist())
				.andExpect(jsonPath("$.paths['/delta/internal/device-auth/validate']").doesNotExist())
				.andExpect(jsonPath("$.components.securitySchemes.mongoServiceAuth").doesNotExist());
	}

}
