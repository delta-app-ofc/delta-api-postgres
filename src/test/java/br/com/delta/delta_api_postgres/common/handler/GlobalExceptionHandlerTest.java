package br.com.delta.delta_api_postgres.common.handler;

import java.sql.SQLException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {
    static Stream<RuntimeException> databaseFailures() {
        return Stream.of(
                new CannotCreateTransactionException("Could not open JPA EntityManager for transaction",
                        new SQLException("Your account or project has exceeded the quota.", "53000")),
                new DataAccessResourceFailureException("Connection to database-host refused")
        );
    }

    @ParameterizedTest
    @MethodSource("databaseFailures")
    void databaseFailuresReturn503WithoutExposingProviderDetails(RuntimeException failure) throws Exception {
        mvc(failure).perform(get("/test/database"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("status").value(503))
                .andExpect(jsonPath("message").value("Banco de dados indisponível. Tente novamente mais tarde."))
                .andExpect(jsonPath("path").value("/test/database"));
    }

    @Test
    void unrelatedFailuresRemainInternalServerErrors() throws Exception {
        mvc(new IllegalStateException("Internal detail")).perform(get("/test/database"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("message").value("Ocorreu um erro interno no servidor."));
    }

    private MockMvc mvc(RuntimeException failure) {
        return MockMvcBuilders.standaloneSetup(new FailingController(failure))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @RestController
    static class FailingController {
        private final RuntimeException failure;

        FailingController(RuntimeException failure) {
            this.failure = failure;
        }

        @GetMapping("/test/database")
        public void database() {
            throw failure;
        }
    }
}
