package br.com.delta.delta_api_postgres.modules.device.repository;

import org.junit.jupiter.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

class DeviceCredentialMigrationTest {
    private Connection connection;
    private String url;

    @BeforeEach void migrateExistingDatabase() throws Exception {
        url = "jdbc:h2:mem:credentials-" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        connection = DriverManager.getConnection(url, "sa", "");
        execute("CREATE TABLE tb_device (id INTEGER PRIMARY KEY, device_id VARCHAR(255) NOT NULL UNIQUE)");
        execute("INSERT INTO tb_device VALUES (7, 'DEVICE-001')");
        new ResourceDatabasePopulator(new ClassPathResource("db/migration/V001__create_device_credentials.sql"))
                .execute(new SingleConnectionDataSource(connection, true));
    }
    @AfterEach void close() throws Exception {
        if (connection != null) {
            execute("DROP ALL OBJECTS");
            connection.close();
        }
    }

    @Test void migrationPreservesExistingDevicesAndDoesNotProvisionKeys() throws Exception {
        assertThat(count("SELECT COUNT(*) FROM tb_device WHERE id = 7 AND device_id = 'DEVICE-001'")).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM tb_device_credential")).isZero();
    }

    @Test void constraintsPreventDuplicateHashesAndKeepRevokedHistory() throws Exception {
        insert(connection, "a".repeat(64));
        assertThatThrownBy(() -> insert(connection, "b".repeat(64))).isInstanceOf(SQLException.class);
        execute("UPDATE tb_device_credential SET revoked_at = TIMESTAMP WITH TIME ZONE '2026-10-05 15:01:00+00', current_device_id = NULL");
        assertThatThrownBy(() -> insert(connection, "a".repeat(64))).isInstanceOf(SQLException.class);
        insert(connection, "b".repeat(64));
        assertThat(count("SELECT COUNT(*) FROM tb_device_credential")).isEqualTo(2);
        execute("DELETE FROM tb_device WHERE id = 7");
        assertThat(count("SELECT COUNT(*) FROM tb_device_credential")).isZero();
    }

    @Test void callersCannotBypassCurrentCredentialConstraintWithNullMarker() throws Exception {
        assertThatThrownBy(() -> execute("INSERT INTO tb_device_credential (device_id, key_hash, created_at) "
                + "VALUES (7, '" + "a".repeat(64) + "', CURRENT_TIMESTAMP)"))
                .isInstanceOf(SQLException.class);
    }

    @Test void foreignKeysAndDateConstraintsRejectInconsistentCredentials() {
        String values = "'" + "a".repeat(64) + "', TIMESTAMP WITH TIME ZONE '2026-10-05 15:00:00+00'";
        assertThatThrownBy(() -> execute("INSERT INTO tb_device_credential (device_id, key_hash, created_at, current_device_id) "
                + "VALUES (8, " + values + ", 8)"))
                .isInstanceOf(SQLException.class);
        assertThatThrownBy(() -> execute("INSERT INTO tb_device_credential (device_id, key_hash, created_at, current_device_id, expires_at) "
                + "VALUES (7, " + values + ", 7, TIMESTAMP WITH TIME ZONE '2026-10-05 15:00:00+00')"))
                .isInstanceOf(SQLException.class);
    }

    @Test void concurrentInsertsAllowOnlyOneCurrentCredential() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Boolean> first = executor.submit(() -> concurrentInsert(start, "a".repeat(64)));
            Future<Boolean> second = executor.submit(() -> concurrentInsert(start, "b".repeat(64)));
            start.countDown();
            assertThat((first.get(10, TimeUnit.SECONDS) ? 1 : 0) + (second.get(10, TimeUnit.SECONDS) ? 1 : 0)).isEqualTo(1);
            assertThat(count("SELECT COUNT(*) FROM tb_device_credential WHERE revoked_at IS NULL")).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test void failedReplacementCanRollBackRevocationInOneTransaction() throws Exception {
        insert(connection, "a".repeat(64));
        connection.setAutoCommit(false);
        try {
            execute("UPDATE tb_device_credential SET revoked_at = TIMESTAMP WITH TIME ZONE '2026-10-05 15:01:00+00', current_device_id = NULL");
            assertThatThrownBy(() -> insert(connection, "invalid-hash")).isInstanceOf(SQLException.class);
            connection.rollback();
        } finally {
            connection.setAutoCommit(true);
        }
        assertThat(count("SELECT COUNT(*) FROM tb_device_credential WHERE revoked_at IS NULL AND current_device_id = 7")).isEqualTo(1);
    }

    private boolean concurrentInsert(CountDownLatch start, String hash) throws Exception {
        try (Connection other = DriverManager.getConnection(url, "sa", "")) {
            start.await();
            try { insert(other, hash); return true; }
            catch (SQLException exception) {
                if (exception.getSQLState().startsWith("23")) return false;
                throw exception;
            }
        }
    }
    private void insert(Connection target, String hash) throws SQLException {
        try (PreparedStatement statement = target.prepareStatement("INSERT INTO tb_device_credential "
                + "(device_id, key_hash, created_at, current_device_id) VALUES (7, ?, ?, 7)")) {
            statement.setString(1, hash);
            statement.setObject(2, java.time.OffsetDateTime.parse("2026-10-05T15:00:00Z"));
            statement.executeUpdate();
        }
    }
    private void execute(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) { statement.execute(sql); }
    }
    private int count(String sql) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery(sql)) {
            rows.next();
            return rows.getInt(1);
        }
    }
}
