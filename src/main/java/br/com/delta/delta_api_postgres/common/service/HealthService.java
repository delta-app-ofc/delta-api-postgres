package br.com.delta.delta_api_postgres.common.service;

import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HealthService {
    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;

    public boolean isHealthy() {
        try (var database = dataSource.getConnection();
             var redis = redisConnectionFactory.getConnection()) {
            return database.isValid(2) && "PONG".equalsIgnoreCase(redis.ping());
        } catch (SQLException | RuntimeException exception) {
            return false;
        }
    }
}
