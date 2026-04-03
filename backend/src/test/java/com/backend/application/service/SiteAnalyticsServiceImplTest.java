package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.application.dto.response.SiteAnalyticsSummaryAppDto;
import com.backend.application.service.config.ConfigPropertyService;
import com.backend.application.service.config.GlobalRuntimeConfigService;
import com.backend.domain.port.SiteAnalyticsPort;
import com.backend.domain.port.TenantContextPort;

@ExtendWith(MockitoExtension.class)
class SiteAnalyticsServiceImplTest {

    @Mock
    private ConfigPropertyService configPropertyService;

    @Mock
    private GlobalRuntimeConfigService globalRuntimeConfigService;

    @Mock
    private TenantContextPort tenantContextPort;

    @Mock
    private SiteAnalyticsPort siteAnalyticsPort;

    private SiteAnalyticsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SiteAnalyticsServiceImpl(
                configPropertyService,
                globalRuntimeConfigService,
                tenantContextPort,
                siteAnalyticsPort);
    }

    @Test
    @DisplayName("getSummary should return disabled when global GA4 flag is off")
    void getSummary_ShouldReturnDisabledWhenFeatureFlagOff() {
        when(globalRuntimeConfigService.getGa4AnalyticsEnabled()).thenReturn(false);

        SiteAnalyticsSummaryAppDto result = service.getSummary();

        assertThat(result.status()).isEqualTo("DISABLED");
        assertThat(result.cards()).isEmpty();
        assertThat(result.trend()).isEmpty();
        verifyNoInteractions(configPropertyService, siteAnalyticsPort);
    }

    @Test
    @DisplayName("getSummary should return not configured when tenant property is missing")
    void getSummary_ShouldReturnNotConfiguredWhenPropertyMissing() {
        stubTenantContext();
        when(globalRuntimeConfigService.getGa4AnalyticsEnabled()).thenReturn(true);
        when(configPropertyService.getBoolean(1L, "ac_tenant_1", "analytics.ga4.enabled", false))
                .thenReturn(true);
        when(configPropertyService.findRaw(1L, "ac_tenant_1", "analytics.ga4.property_id"))
                .thenReturn(Optional.empty());

        SiteAnalyticsSummaryAppDto result = service.getSummary();

        assertThat(result.status()).isEqualTo("NOT_CONFIGURED");
        assertThat(result.propertyId()).isNull();
        assertThat(result.cards()).isEmpty();
    }

    @Test
    @DisplayName("getSummary should return access error when tenant property is not numeric")
    void getSummary_ShouldReturnAccessErrorWhenPropertyIdInvalid() {
        stubTenantContext();
        when(globalRuntimeConfigService.getGa4AnalyticsEnabled()).thenReturn(true);
        when(configPropertyService.getBoolean(1L, "ac_tenant_1", "analytics.ga4.enabled", false))
                .thenReturn(true);
        when(configPropertyService.findRaw(1L, "ac_tenant_1", "analytics.ga4.property_id"))
                .thenReturn(Optional.of("G-TEST123"));

        SiteAnalyticsSummaryAppDto result = service.getSummary();

        assertThat(result.status()).isEqualTo("ACCESS_ERROR");
        assertThat(result.propertyId()).isEqualTo("G-TEST123");
        verifyNoInteractions(siteAnalyticsPort);
    }

    @Test
    @DisplayName("getSummary should return ready payload with ordered KPI cards")
    void getSummary_ShouldReturnReadyPayload() {
        stubTenantContext();
        when(globalRuntimeConfigService.getGa4AnalyticsEnabled()).thenReturn(true);
        when(configPropertyService.getBoolean(1L, "ac_tenant_1", "analytics.ga4.enabled", false))
                .thenReturn(true);
        when(configPropertyService.findRaw(1L, "ac_tenant_1", "analytics.ga4.property_id"))
                .thenReturn(Optional.of("123456789"));
        when(siteAnalyticsPort.fetchSummary(
                eq("123456789"),
                eq(SiteAnalyticsPort.AnalyticsRange.LAST_7_DAYS)))
                .thenReturn(new SiteAnalyticsPort.AnalyticsSummary(
                        metrics(42D, 180D, 12D, 0.61D),
                        metrics(30D, 160D, 8D, 0.54D),
                        List.of(
                                new SiteAnalyticsPort.TrendPoint(LocalDate.of(2026, 3, 25), 4D),
                                new SiteAnalyticsPort.TrendPoint(LocalDate.of(2026, 3, 26), 7D)),
                        Instant.parse("2026-04-01T10:15:30Z")));

        SiteAnalyticsSummaryAppDto result = service.getSummary();

        assertThat(result.status()).isEqualTo("READY");
        assertThat(result.propertyId()).isEqualTo("123456789");
        assertThat(result.cards()).hasSize(4);
        assertThat(result.cards().get(0).metric()).isEqualTo("activeUsers");
        assertThat(result.cards().get(0).deltaDirection()).isEqualTo("up");
        assertThat(result.cards().get(0).deltaPercentage()).isEqualTo(40D);
        assertThat(result.trend()).hasSize(2);
        assertThat(result.lastSyncedAt()).isNotNull();
    }

    @Test
    @DisplayName("getSummary should return no data when metrics and trend are empty")
    void getSummary_ShouldReturnNoDataWhenEverythingIsZero() {
        stubTenantContext();
        when(globalRuntimeConfigService.getGa4AnalyticsEnabled()).thenReturn(true);
        when(configPropertyService.getBoolean(1L, "ac_tenant_1", "analytics.ga4.enabled", false))
                .thenReturn(true);
        when(configPropertyService.findRaw(1L, "ac_tenant_1", "analytics.ga4.property_id"))
                .thenReturn(Optional.of("123456789"));
        when(siteAnalyticsPort.fetchSummary(
                eq("123456789"),
                eq(SiteAnalyticsPort.AnalyticsRange.LAST_7_DAYS)))
                .thenReturn(new SiteAnalyticsPort.AnalyticsSummary(
                        metrics(0D, 0D, 0D, 0D),
                        metrics(0D, 0D, 0D, 0D),
                        List.of(
                                new SiteAnalyticsPort.TrendPoint(LocalDate.of(2026, 3, 25), 0D),
                                new SiteAnalyticsPort.TrendPoint(LocalDate.of(2026, 3, 26), 0D)),
                        Instant.parse("2026-04-01T10:15:30Z")));

        SiteAnalyticsSummaryAppDto result = service.getSummary();

        assertThat(result.status()).isEqualTo("NO_DATA");
        assertThat(result.cards()).isEmpty();
        assertThat(result.trend()).isEmpty();
    }

    @Test
    @DisplayName("getSummary should return access error when analytics port fails")
    void getSummary_ShouldReturnAccessErrorWhenPortFails() {
        stubTenantContext();
        when(globalRuntimeConfigService.getGa4AnalyticsEnabled()).thenReturn(true);
        when(configPropertyService.getBoolean(1L, "ac_tenant_1", "analytics.ga4.enabled", false))
                .thenReturn(true);
        when(configPropertyService.findRaw(1L, "ac_tenant_1", "analytics.ga4.property_id"))
                .thenReturn(Optional.of("123456789"));
        when(siteAnalyticsPort.fetchSummary(
                eq("123456789"),
                eq(SiteAnalyticsPort.AnalyticsRange.LAST_7_DAYS)))
                .thenThrow(new IllegalStateException("boom"));

        SiteAnalyticsSummaryAppDto result = service.getSummary();

        assertThat(result.status()).isEqualTo("ACCESS_ERROR");
        assertThat(result.propertyId()).isEqualTo("123456789");
        assertThat(result.cards()).isEmpty();
    }

    @Test
    @DisplayName("getSummary should return disabled when tenant GA4 flag is off")
    void getSummary_ShouldReturnDisabledWhenTenantFlagOff() {
        stubTenantContext();
        when(globalRuntimeConfigService.getGa4AnalyticsEnabled()).thenReturn(true);
        when(configPropertyService.getBoolean(1L, "ac_tenant_1", "analytics.ga4.enabled", false))
                .thenReturn(false);

        SiteAnalyticsSummaryAppDto result = service.getSummary();

        assertThat(result.status()).isEqualTo("DISABLED");
        assertThat(result.cards()).isEmpty();
        assertThat(result.trend()).isEmpty();
    }

    private Map<String, Double> metrics(
            double activeUsers,
            double screenPageViews,
            double newUsers,
            double engagementRate
    ) {
        Map<String, Double> values = new LinkedHashMap<>();
        values.put("activeUsers", activeUsers);
        values.put("screenPageViews", screenPageViews);
        values.put("newUsers", newUsers);
        values.put("engagementRate", engagementRate);
        return values;
    }

    private void stubTenantContext() {
        when(tenantContextPort.getTenantId()).thenReturn("1");
        when(tenantContextPort.getTenantDbName()).thenReturn("ac_tenant_1");
    }
}
