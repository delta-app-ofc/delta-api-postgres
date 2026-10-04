package br.com.delta.delta_api_postgres;

import br.com.delta.delta_api_postgres.modules.auth.entity.AuthUser;
import br.com.delta.delta_api_postgres.modules.auth.repository.AuthUserRepository;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTests extends AuthTestConfig {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired AuthUserRepository users;
    @Autowired PasswordEncoder passwords;
    @MockitoBean StringRedisTemplate redisTemplate;
    private final ValueOperations<String, String> redisValues = mock(ValueOperations.class);

    @BeforeEach void setup() {
        when(redisTemplate.opsForValue()).thenReturn(redisValues);
        users.deleteAll();
        var user = new AuthUser();
        user.setName("Existing User");
        user.setEmail("user@example.com");
        user.setPasswordHash(passwords.encode("test-password-123"));
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        user.setEnabled(true);
        users.saveAndFlush(user);
    }

    @Test void createAccountUsesUserTableAndStartsSession() throws Exception {
        var response = mvc.perform(post("/delta/auth/create-account").contentType("application/json").content("""
                {"name":"Davi","email":"DAVI@gmail.com","password":"senha1234",
                 "phone":null,"birthDate":"2000-01-01"}
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("accessToken").isNotEmpty())
                .andExpect(jsonPath("tokenType").value("Bearer"))
                .andReturn().getResponse();

        var user = users.findByEmail("davi@gmail.com").orElseThrow();
        assertThat(user.getName()).isEqualTo("Davi");
        assertThat(user.getPasswordHash()).isNotEqualTo("senha1234");
        var token = json.readTree(response.getContentAsString()).path("accessToken").asText();
        mvc.perform(get("/delta/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("email").value("davi@gmail.com"));
    }

    @Test void loginReturnsAUsableToken() throws Exception {
        var response = mvc.perform(post("/delta/auth/login").contentType("application/json")
                        .content("{\"email\":\"USER@example.com\",\"password\":\"test-password-123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse();
        var token = json.readTree(response.getContentAsString()).path("accessToken").asText();
        mvc.perform(get("/delta/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("name").value("Existing User"));
    }

    @Test void invalidRegistrationAndDuplicateEmailAreRejected() throws Exception {
        mvc.perform(post("/delta/auth/create-account").contentType("application/json")
                        .content("{\"name\":\"Davi\",\"email\":\"user@example.com\",\"password\":\"senha1234\",\"birthDate\":\"2000-01-01\"}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/delta/auth/create-account").contentType("application/json")
                        .content("{\"name\":\"\",\"email\":\"invalid\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test void inactiveUserAndAnonymousRequestsAreRejected() throws Exception {
        mvc.perform(get("/delta/auth/me")).andExpect(status().isUnauthorized());
        var user = users.findByEmail("user@example.com").orElseThrow();
        user.setEnabled(false);
        users.saveAndFlush(user);
        mvc.perform(post("/delta/auth/login").contentType("application/json")
                        .content("{\"email\":\"user@example.com\",\"password\":\"test-password-123\"}"))
                .andExpect(status().isUnauthorized());
    }
    @Test void createAccountIssuesUsableTokens() throws Exception {
        var response = mvc.perform(post("/delta/auth/create-account").contentType("application/json").content("""
                {"name":"Mobile","email":"mobile@example.com","password":"senha1234","birthDate":"2000-01-01"}
                """))
                .andExpect(status().isCreated()).andExpect(jsonPath("refreshToken").isNotEmpty())
                .andReturn().getResponse();
        var token = json.readTree(response.getContentAsString()).path("accessToken").asText();
        mvc.perform(get("/delta/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("email").value("mobile@example.com"));
    }

    @Test void completionRequiresAuthenticationAndValidNestedFields() throws Exception {
        mvc.perform(post("/delta/auth/complete-registration").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/delta/auth/complete-registration")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt())
                .contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
    }
}
