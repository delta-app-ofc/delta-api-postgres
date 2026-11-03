package br.com.delta.delta_api_postgres.modules.auth.security;

import br.com.delta.delta_api_postgres.modules.auth.config.AuthProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRateLimitFilterTest {
    private static final String LOGIN = "/delta/auth/login";

    @Test
    void blocksTheFirstRequestAboveTheConfiguredLimitWithRetryMetadata() throws Exception {
        var properties = properties(2);
        var filter = filter(properties, new AdjustableClock(Instant.parse("2026-10-31T12:34:00Z")));
        var downstreamCalls = new AtomicInteger();

        perform(filter, request("POST", LOGIN, "192.0.2.10"), downstreamCalls);
        perform(filter, request("POST", LOGIN, "192.0.2.10"), downstreamCalls);
        var response = perform(filter, request("POST", LOGIN, "192.0.2.10"), downstreamCalls);

        assertThat(downstreamCalls).hasValue(2);
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isEqualTo("60");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(response.getContentType()).startsWith("application/json");
        var body = new ObjectMapper().readTree(response.getContentAsString());
        assertThat(body.path("status").asInt()).isEqualTo(429);
        assertThat(body.path("message").asText()).contains("Muitas tentativas");
        assertThat(body.path("path").asText()).isEqualTo(LOGIN);
    }

    @Test
    void startsANewQuotaWhenTheMinuteChanges() throws Exception {
        var clock = new AdjustableClock(Instant.parse("2026-10-31T12:34:00Z"));
        var filter = filter(properties(1), clock);
        var downstreamCalls = new AtomicInteger();

        perform(filter, request("POST", LOGIN, "192.0.2.11"), downstreamCalls);
        var blocked = perform(filter, request("POST", LOGIN, "192.0.2.11"), downstreamCalls);
        clock.advanceSeconds(60);
        var allowed = perform(filter, request("POST", LOGIN, "192.0.2.11"), downstreamCalls);

        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(allowed.getStatus()).isEqualTo(200);
        assertThat(downstreamCalls).hasValue(2);
    }

    @Test
    void leavesOtherMethodsAndPathsOutsideTheAuthenticationQuota() throws Exception {
        var filter = filter(properties(1), new AdjustableClock(Instant.parse("2026-10-31T12:34:00Z")));
        var downstreamCalls = new AtomicInteger();

        var getResponse = perform(filter, request("GET", LOGIN, "192.0.2.12"), downstreamCalls);
        var unrelatedResponse = perform(filter, request("POST", "/delta/properties", "192.0.2.12"), downstreamCalls);
        var firstLogin = perform(filter, request("POST", LOGIN, "192.0.2.12"), downstreamCalls);
        var secondLogin = perform(filter, request("POST", LOGIN, "192.0.2.12"), downstreamCalls);

        assertThat(getResponse.getStatus()).isEqualTo(200);
        assertThat(unrelatedResponse.getStatus()).isEqualTo(200);
        assertThat(firstLogin.getStatus()).isEqualTo(200);
        assertThat(secondLogin.getStatus()).isEqualTo(429);
        assertThat(downstreamCalls).hasValue(3);
    }

    @Test
    void recognizesAuthenticationPathsWhenTheApplicationHasAContextPath() throws Exception {
        var filter = filter(properties(1), new AdjustableClock(Instant.parse("2026-10-31T12:34:00Z")));
        var downstreamCalls = new AtomicInteger();

        var request = request("POST", "/delta/auth/login", "192.0.2.13");
        request.setContextPath("/app");
        request.setRequestURI("/app/delta/auth/login");

        perform(filter, request, downstreamCalls);
        var blocked = perform(filter, request("POST", "/delta/auth/login", "192.0.2.13"), downstreamCalls);

        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(downstreamCalls).hasValue(1);
    }

    private static AuthProperties properties(int attempts) {
        var properties = new AuthProperties();
        properties.setAttemptsPerMinute(attempts);
        return properties;
    }

    private static AuthRateLimitFilter filter(AuthProperties properties, Clock clock) {
        var errors = new SecurityErrorHandler(new ObjectMapper().registerModule(new JavaTimeModule()));
        return new AuthRateLimitFilter(properties, errors, clock);
    }

    private static MockHttpServletRequest request(String method, String path, String remoteAddress) {
        var request = new MockHttpServletRequest(method, path);
        request.setRemoteAddr(remoteAddress);
        return request;
    }

    private static MockHttpServletResponse perform(
            AuthRateLimitFilter filter,
            MockHttpServletRequest request,
            AtomicInteger downstreamCalls) throws ServletException, IOException {
        var response = new MockHttpServletResponse();
        FilterChain chain = (ignoredRequest, ignoredResponse) -> downstreamCalls.incrementAndGet();
        filter.doFilter(request, response, chain);
        return response;
    }

    private static final class AdjustableClock extends Clock {
        private Instant current;

        private AdjustableClock(Instant current) {
            this.current = current;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return current;
        }

        private void advanceSeconds(long seconds) {
            current = current.plusSeconds(seconds);
        }
    }
}
