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

import com.backend.application.dto.response.SiteInsightsSummaryAppDto;
import com.backend.application.service.config.ConfigPropertyService;
import com.backend.application.service.config.GlobalRuntimeConfigService;
import com.backend.domain.entity.Tenant;
import com.backend.domain.port.FrontendConfigPort;
import com.backend.domain.port.SitePerformanceInsightsPort;
import com.backend.domain.port.SiteSeoInsightsPort;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.TenantRepository;

@ExtendWith(MockitoExtension.class)
class SiteInsightsServiceImplTest {

    @Mock
    private ConfigPropertyService configPropertyService;

    @Mock
    private GlobalRuntimeConfigService globalRuntimeConfigService;

    @Mock
    private TenantContextPort tenantContextPort;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private FrontendConfigPort frontendConfigPort;

    @Mock
    private SiteSeoInsightsPort siteSeoInsightsPort;

    @Mock
    private SitePerformanceInsightsPort sitePerformanceInsightsPort;

    private SiteInsightsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SiteInsightsServiceImpl(
                configPropertyService,
                globalRuntimeConfigService,
                tenantContextPort,
                tenantRepository,
                frontendConfigPort,
                siteSeoInsightsPort,
                sitePerformanceInsightsPort);
    }

    @Test
    @DisplayName("getSummary should return disabled when global SEO insights flag is off")
    void getSummary_ShouldReturnDisabledWhenGlobalFlagOff() {
        when(globalRuntimeConfigService.getSeoInsightsEnabled()).thenReturn(false);

        SiteInsightsSummaryAppDto result = service.getSummary();

        assertThat(result.seo().status()).isEqualTo("DISABLED");
        assertThat(result.performance().status()).isEqualTo("DISABLED");
        verifyNoInteractions(configPropertyService, siteSeoInsightsPort, sitePerformanceInsightsPort);
    }

    @Test
    @DisplayName("getSummary should return not configured seo and ready performance when property is missing")
    void getSummary_ShouldReturnNotConfiguredSeoAndReadyPerformanceWhenPropertyMissing() {
        stubTenantContext();
        when(globalRuntimeConfigService.getSeoInsightsEnabled()).thenReturn(true);
        when(configPropertyService.getBoolean(1L, "ac_tenant_1", "seo.insights.enabled", false))
                .thenReturn(true);
        when(configPropertyService.findRaw(1L, "ac_tenant_1", "seo.search_console.property_url"))
                .thenReturn(Optional.empty());
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant("www.example.com")));
        when(sitePerformanceInsightsPort.fetchHistory(
                eq("https://www.example.com"),
                eq(SitePerformanceInsightsPort.TargetScope.URL),
                eq("DESKTOP"),
                eq(List.of(
                        "largest_contentful_paint",
                        "interaction_to_next_paint",
                        "cumulative_layout_shift",
                        "experimental_time_to_first_byte")),
                eq(6)))
                .thenReturn(Optional.of(performanceHistory(
                        SitePerformanceInsightsPort.TargetScope.URL,
                        "https://www.example.com")));

        SiteInsightsSummaryAppDto result = service.getSummary();

        assertThat(result.seo().status()).isEqualTo("NOT_CONFIGURED");
        assertThat(result.performance().status()).isEqualTo("READY");
        assertThat(result.performance().targetScope()).isEqualTo("URL");
    }

    @Test
    @DisplayName("getSummary should fallback performance to origin when url history is missing")
    void getSummary_ShouldFallbackPerformanceToOrigin() {
        stubTenantContext();
        when(globalRuntimeConfigService.getSeoInsightsEnabled()).thenReturn(true);
        when(configPropertyService.getBoolean(1L, "ac_tenant_1", "seo.insights.enabled", false))
                .thenReturn(true);
        when(configPropertyService.findRaw(1L, "ac_tenant_1", "seo.search_console.property_url"))
                .thenReturn(Optional.of("sc-domain:example.com"));
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant("www.example.com")));
        when(siteSeoInsightsPort.fetchSearchPerformance(
                eq("sc-domain:example.com"),
                eq(SiteSeoInsightsPort.SearchRange.LAST_28_DAYS)))
                .thenReturn(new SiteSeoInsightsPort.SearchPerformanceSummary(
                        seoMetrics(40D, 800D, 0.05D, 9.2D),
                        seoMetrics(30D, 650D, 0.04D, 10.5D),
                        List.of(new SiteSeoInsightsPort.SearchTrendPoint(LocalDate.of(2026, 4, 1), 10D, 200D)),
                        Instant.parse("2026-04-02T08:00:00Z")));
        when(siteSeoInsightsPort.inspectUrl("sc-domain:example.com", "https://www.example.com"))
                .thenReturn(new SiteSeoInsightsPort.UrlInspectionSummary(
                        "PASS",
                        "Submitted and indexed",
                        "ALLOWED",
                        "INDEXING_ALLOWED",
                        "SUCCESSFUL",
                        Instant.parse("2026-04-02T07:30:00Z"),
                        "https://www.example.com",
                        "https://www.example.com",
                        List.of("https://www.example.com/sitemap.xml"),
                        Instant.parse("2026-04-02T08:00:00Z")));
        when(sitePerformanceInsightsPort.fetchHistory(
                eq("https://www.example.com"),
                eq(SitePerformanceInsightsPort.TargetScope.URL),
                eq("DESKTOP"),
                eq(List.of(
                        "largest_contentful_paint",
                        "interaction_to_next_paint",
                        "cumulative_layout_shift",
                        "experimental_time_to_first_byte")),
                eq(6)))
                .thenReturn(Optional.empty());
        when(sitePerformanceInsightsPort.fetchHistory(
                eq("https://www.example.com"),
                eq(SitePerformanceInsightsPort.TargetScope.ORIGIN),
                eq("DESKTOP"),
                eq(List.of(
                        "largest_contentful_paint",
                        "interaction_to_next_paint",
                        "cumulative_layout_shift",
                        "experimental_time_to_first_byte")),
                eq(6)))
                .thenReturn(Optional.of(performanceHistory(
                        SitePerformanceInsightsPort.TargetScope.ORIGIN,
                        "https://www.example.com")));

        SiteInsightsSummaryAppDto result = service.getSummary();

        assertThat(result.seo().status()).isEqualTo("READY");
        assertThat(result.performance().status()).isEqualTo("READY");
        assertThat(result.performance().targetScope()).isEqualTo("ORIGIN");
        assertThat(result.performance().metrics()).hasSize(4);
        assertThat(result.performance().score()).isNotNull();
    }

    private Map<String, Double> seoMetrics(
            double clicks,
            double impressions,
            double ctr,
            double position
    ) {
        Map<String, Double> values = new LinkedHashMap<>();
        values.put("clicks", clicks);
        values.put("impressions", impressions);
        values.put("ctr", ctr);
        values.put("position", position);
        return values;
    }

    private SitePerformanceInsightsPort.PerformanceHistory performanceHistory(
            SitePerformanceInsightsPort.TargetScope targetScope,
            String target
    ) {
        Map<String, List<Double>> metricSeries = new LinkedHashMap<>();
        metricSeries.put("largest_contentful_paint", List.of(2100D, 2050D, 1980D));
        metricSeries.put("interaction_to_next_paint", List.of(180D, 190D, 175D));
        metricSeries.put("cumulative_layout_shift", List.of(0.08D, 0.07D, 0.06D));
        metricSeries.put("experimental_time_to_first_byte", List.of(700D, 680D, 660D));
        return new SitePerformanceInsightsPort.PerformanceHistory(
                targetScope,
                target,
                "DESKTOP",
                metricSeries,
                List.of(
                        new SitePerformanceInsightsPort.CollectionPeriod(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28)),
                        new SitePerformanceInsightsPort.CollectionPeriod(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 28)),
                        new SitePerformanceInsightsPort.CollectionPeriod(LocalDate.of(2026, 3, 7), LocalDate.of(2026, 4, 3))),
                Instant.parse("2026-04-03T09:15:00Z"));
    }

    private Tenant tenant(String customDomain) {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setSubdomain("demo");
        tenant.setCustomDomain(customDomain);
        return tenant;
    }

    private void stubTenantContext() {
        when(tenantContextPort.getTenantId()).thenReturn("1");
        when(tenantContextPort.getTenantDbName()).thenReturn("ac_tenant_1");
    }
}
