package br.com.delta.delta_api_postgres.modules.device.repository;

import br.com.delta.delta_api_postgres.modules.device.entity.DeviceCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import java.time.Instant;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:credential-repository;MODE=PostgreSQL;NON_KEYWORDS=MONTH;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DeviceCredentialRepositoryTest {
    @Autowired DeviceCredentialRepository credentials;
    @Autowired DeviceRepository devices;
    @Autowired DataSource dataSource;
    @Autowired jakarta.persistence.EntityManager entityManager;
    private static final Instant CREATED = Instant.parse("2026-10-05T15:00:00Z");

    @BeforeEach void seedExistingDevice() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update("INSERT INTO tb_region (id, name) VALUES (1000, 'GRANDE_SP')");
        jdbc.update("INSERT INTO tb_address (id, region_id, cep, city, state) VALUES (1000, 1000, '01001000', 'São Paulo', 'SP')");
        jdbc.update("INSERT INTO tb_property_classification (id, name, group_name) VALUES (1000, 'RESIDENCIAL_NORMAL', 'RESIDENCIAL')");
        jdbc.update("INSERT INTO tb_property (id, name, type, classification_id, address_id, registration_date) "
                + "VALUES (1000, 'Casa', 'CASA', 1000, 1000, DATE '2026-01-01')");
        jdbc.update("INSERT INTO tb_device (id, device_id, property_id, is_active, installation_date) "
                + "VALUES (1000, 'DEVICE-001', 1000, TRUE, DATE '2026-01-01')");
    }

    @Test void persistedCredentialKeepsCanonicalIdentityAndUtcDates() {
        var device = devices.findLockedById(1000).orElseThrow();
        var credential = credentials.saveAndFlush(new DeviceCredential(device, "a".repeat(64), CREATED, CREATED.plusSeconds(60)));
        entityManager.clear();

        assertThat(credential.getId()).isNotNull();
        var persisted = credentials.findByKeyHash("a".repeat(64)).orElseThrow();
        assertThat(persisted.getDevice().getDeviceId()).isEqualTo("DEVICE-001");
        assertThat(persisted.getCreatedAt()).isEqualTo(CREATED);
        assertThat(persisted.getExpiresAt()).isEqualTo(CREATED.plusSeconds(60));
        assertThat(credentials.findByCurrentDeviceId(1000)).isPresent();
    }

    @Test void revocationReleasesCurrentSlotWithoutDeletingHistory() {
        var device = devices.findById(1000).orElseThrow();
        var old = credentials.saveAndFlush(new DeviceCredential(device, "a".repeat(64), CREATED, null));
        old.revoke(CREATED.plusSeconds(60));
        credentials.flush();
        var current = credentials.saveAndFlush(new DeviceCredential(device, "b".repeat(64), CREATED.plusSeconds(60), null));

        assertThat(credentials.findByCurrentDeviceId(1000).orElseThrow().getId()).isEqualTo(current.getId());
        assertThat(credentials.findByKeyHash("a".repeat(64)).orElseThrow().isValidAt(CREATED.plusSeconds(61))).isFalse();
        assertThat(credentials.findByDevice_IdOrderByCreatedAtDescIdDesc(1000))
                .extracting(DeviceCredential::getId).containsExactly(current.getId(), old.getId());
    }
}
