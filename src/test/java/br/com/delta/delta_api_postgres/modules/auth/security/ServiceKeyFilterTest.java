package br.com.delta.delta_api_postgres.modules.auth.security;

import br.com.delta.delta_api_postgres.modules.auth.config.AuthProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceKeyFilterTest {
    private static final String OWNER = "/delta/internal/device/DEVICE-001/owner";

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void letsTheRequestThroughAndGrantsTheServiceRoleWhenTheKeyMatches() throws Exception {
        var calls = new AtomicInteger();
        var request = request(OWNER, "chave-secreta");

        var response = perform(filter("chave-secreta"), request, calls);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(calls).hasValue(1);
    }

    @Test
    void grantsOnlyTheServiceRoleDuringTheRequest() throws Exception {
        var roles = new java.util.ArrayList<String>();
        var filter = filter("chave-secreta");
        var request = request(OWNER, "chave-secreta");

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) ->
                SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                        .forEach(authority -> roles.add(authority.getAuthority())));

        assertThat(roles).containsExactly("ROLE_" + ServiceKeyFilter.ROLE);
    }

    @Test
    void rejectsAWrongKeyWithoutCallingTheController() throws Exception {
        var calls = new AtomicInteger();

        var response = perform(filter("chave-secreta"), request(OWNER, "outra-chave"), calls);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(calls).hasValue(0);
        assertThat(response.getContentAsString()).contains("Chave de serviço");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void rejectsARequestWithoutTheKeyHeader() throws Exception {
        var calls = new AtomicInteger();

        var response = perform(filter("chave-secreta"), request(OWNER, null), calls);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(calls).hasValue(0);
    }

    @Test
    void keepsInternalRoutesClosedWhenNoKeyIsConfigured() throws Exception {
        var calls = new AtomicInteger();

        var emptyKey = perform(filter(""), request(OWNER, ""), calls);
        var anyKey = perform(filter(""), request(OWNER, "qualquer"), calls);

        assertThat(emptyKey.getStatus()).isEqualTo(401);
        assertThat(anyKey.getStatus()).isEqualTo(401);
        assertThat(calls).hasValue(0);
    }

    @Test
    void ignoresRoutesOutsideTheInternalPrefix() throws Exception {
        var calls = new AtomicInteger();

        var response = perform(filter("chave-secreta"), request("/delta/device/1", "chave-secreta"), calls);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(calls).hasValue(1);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void recognizesInternalRoutesWhenTheApplicationHasAContextPath() throws Exception {
        var calls = new AtomicInteger();
        var request = request(OWNER, "errada");
        request.setContextPath("/app");
        request.setRequestURI("/app" + OWNER);

        var response = perform(filter("chave-secreta"), request, calls);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(calls).hasValue(0);
    }

    private static ServiceKeyFilter filter(String key) {
        var properties = new AuthProperties();
        properties.setServiceKey(key);
        var errors = new SecurityErrorHandler(new ObjectMapper().registerModule(new JavaTimeModule()));
        return new ServiceKeyFilter(properties, errors);
    }

    private static MockHttpServletRequest request(String path, String key) {
        var request = new MockHttpServletRequest("GET", path);
        if (key != null) request.addHeader(ServiceKeyFilter.HEADER, key);
        return request;
    }

    private static MockHttpServletResponse perform(ServiceKeyFilter filter, MockHttpServletRequest request,
                                                   AtomicInteger calls) throws Exception {
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> calls.incrementAndGet());
        return response;
    }
}
