package br.com.delta.delta_api_postgres.common.service;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.RedisConnectionFailureException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HealthServiceTest {
    private final DataSource dataSource = mock(DataSource.class);
    private final RedisConnectionFactory redisFactory = mock(RedisConnectionFactory.class);
    private final HealthService healthService = new HealthService(dataSource, redisFactory);

    @Test
    void returnsUpWhenDatabaseAndRedisRespond() throws Exception {
        Connection database = mock(Connection.class);
        RedisConnection redis = mock(RedisConnection.class);
        when(dataSource.getConnection()).thenReturn(database);
        when(redisFactory.getConnection()).thenReturn(redis);
        when(database.isValid(2)).thenReturn(true);
        when(redis.ping()).thenReturn("PONG");

        assertThat(healthService.isHealthy()).isTrue();
    }

    @Test
    void returnsDownWhenDatabaseFails() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException("database unavailable"));

        assertThat(healthService.isHealthy()).isFalse();
    }

    @Test
    void returnsDownWhenRedisFails() throws Exception {
        Connection database = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(database);
        when(redisFactory.getConnection()).thenThrow(new RedisConnectionFailureException("redis unavailable"));

        assertThat(healthService.isHealthy()).isFalse();
    }

    @Test
    void returnsDownWhenDatabaseConnectionIsInvalid() throws Exception {
        Connection database = mock(Connection.class);
        RedisConnection redis = mock(RedisConnection.class);
        when(dataSource.getConnection()).thenReturn(database);
        when(redisFactory.getConnection()).thenReturn(redis);
        when(database.isValid(2)).thenReturn(false);
        when(redis.ping()).thenReturn("PONG");

        assertThat(healthService.isHealthy()).isFalse();
    }

    @Test
    void returnsDownWhenRedisDoesNotReturnPong() throws Exception {
        Connection database = mock(Connection.class);
        RedisConnection redis = mock(RedisConnection.class);
        when(dataSource.getConnection()).thenReturn(database);
        when(redisFactory.getConnection()).thenReturn(redis);
        when(database.isValid(2)).thenReturn(true);
        when(redis.ping()).thenReturn("LOADING");

        assertThat(healthService.isHealthy()).isFalse();
    }
}
