package br.com.delta.delta_api_postgres.modules.analytics.service;

import br.com.delta.delta_api_postgres.modules.analytics.dto.io.DeviceSummaryIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.LeakIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.PropertyMonthlyIO;
import br.com.delta.delta_api_postgres.modules.analytics.repository.AnalyticsRepository;
import br.com.delta.delta_api_postgres.modules.analytics.repository.projection.DeviceSummaryProjection;
import br.com.delta.delta_api_postgres.modules.analytics.repository.projection.LeakProjection;
import br.com.delta.delta_api_postgres.modules.analytics.repository.projection.PropertyMonthlyProjection;
import br.com.delta.delta_api_postgres.modules.auth.security.UserDataAccess;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {
    private static final Integer USER_ID = 7;

    @Mock private UserDataAccess access;
    @Mock private AnalyticsRepository analyticsRepository;
    @InjectMocks private AnalyticsService service;

    @Test
    void scopesPropertyMonthlyToTheAuthenticatedUserAndMapsEveryField() {
        PropertyMonthlyProjection row = mock(PropertyMonthlyProjection.class);
        when(row.getPropertyId()).thenReturn(3);
        when(row.getPropertyName()).thenReturn("LOJA SWIFT - MOEMA");
        when(row.getReferenceMonth()).thenReturn(LocalDate.of(2026, 10, 1));
        when(row.getTotalLiters()).thenReturn(new BigDecimal("4026.540"));
        when(row.getGoalPct()).thenReturn(new BigDecimal("53.4"));
        when(row.getGoalStatus()).thenReturn("OK");
        when(row.getIsCompleteMonth()).thenReturn(true);
        when(access.userId()).thenReturn(USER_ID);
        when(analyticsRepository.findPropertyMonthly(USER_ID, LocalDate.of(2026, 10, 1))).thenReturn(List.of(row));

        List<PropertyMonthlyIO> result = service.findPropertyMonthly(LocalDate.of(2026, 10, 1));

        assertThat(result).singleElement().satisfies(item -> {
            assertThat(item.propertyId()).isEqualTo(3);
            assertThat(item.propertyName()).isEqualTo("LOJA SWIFT - MOEMA");
            assertThat(item.goalStatus()).isEqualTo("OK");
            assertThat(item.goalPct()).isEqualByComparingTo("53.4");
            assertThat(item.isCompleteMonth()).isTrue();
        });
    }

    @Test
    void passesTheLeakStatusFilterToTheRepository() {
        LeakProjection row = mock(LeakProjection.class);
        when(row.getLeakStatus()).thenReturn("ATIVO");
        when(row.getTriggeredAt()).thenReturn(LocalDateTime.of(2026, 11, 2, 21, 40));
        when(access.userId()).thenReturn(USER_ID);
        when(analyticsRepository.findLeaks(USER_ID, "ATIVO")).thenReturn(List.of(row));

        List<LeakIO> result = service.findLeaks("ATIVO");

        assertThat(result).singleElement().satisfies(item -> assertThat(item.leakStatus()).isEqualTo("ATIVO"));
        verify(analyticsRepository).findLeaks(USER_ID, "ATIVO");
    }

    @Test
    void returnsEmptyDeviceSummaryWhenUserHasNoOrganization() {
        when(access.userId()).thenReturn(USER_ID);
        when(analyticsRepository.findDeviceSummary(USER_ID)).thenReturn(List.of());

        assertThat(service.findDeviceSummary()).isEmpty();
    }

    @Test
    void mapsDeviceSummaryCounters() {
        DeviceSummaryProjection row = mock(DeviceSummaryProjection.class);
        when(row.getOrganizationId()).thenReturn(1);
        when(row.getDevicesPaired()).thenReturn(10L);
        when(row.getDevicesOnline()).thenReturn(9L);
        when(row.getDevicesLowBattery()).thenReturn(2L);
        when(access.userId()).thenReturn(USER_ID);
        when(analyticsRepository.findDeviceSummary(USER_ID)).thenReturn(List.of(row));

        List<DeviceSummaryIO> result = service.findDeviceSummary();

        assertThat(result).singleElement().satisfies(item -> {
            assertThat(item.devicesPaired()).isEqualTo(10L);
            assertThat(item.devicesOnline()).isEqualTo(9L);
            assertThat(item.devicesLowBattery()).isEqualTo(2L);
        });
    }
}
