package com.backend.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.backend.application.dto.response.SiteAnalyticsSummaryAppDto;
import com.backend.application.service.analytics.SiteDataStatus;
import com.backend.application.service.config.ConfigPropertyService;
import com.backend.application.service.config.GlobalRuntimeConfigService;
import com.backend.domain.port.SiteAnalyticsPort;
import com.backend.domain.port.TenantContextPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SiteAnalyticsServiceImpl implements SiteAnalyticsService {

    static final String RANGE_LAST_7_DAYS = "LAST_7_DAYS";
    static final String KEY_GA4_ENABLED = "analytics.ga4.enabled";
    static final String KEY_GA4_PROPERTY_ID = "analytics.ga4.property_id";

    private static final Pattern PROPERTY_ID_PATTERN = Pattern.compile("^\\d+$");
    private static final List<String> KPI_ORDER = List.of(
            "activeUsers",
            "screenPageViews",
            "newUsers",
            "engagementRate");

    private final ConfigPropertyService configPropertyService;
    private final GlobalRuntimeConfigService globalRuntimeConfigService;
    private final TenantContextPort tenantContextPort;
    private final SiteAnalyticsPort siteAnalyticsPort;

    @Override
    public SiteAnalyticsSummaryAppDto getSummary() {
        if (!Boolean.TRUE.equals(globalRuntimeConfigService.getGa4AnalyticsEnabled())) {
            return emptySummary(
                    SiteDataStatus.DISABLED,
                    null,
                    null);
        }

        if (!isTenantAnalyticsEnabled()) {
            return emptySummary(
                    SiteDataStatus.DISABLED,
                    null,
                    null);
        }

        String propertyId = resolvePropertyId();
        if (!StringUtils.hasText(propertyId)) {
            return emptySummary(
                    SiteDataStatus.NOT_CONFIGURED,
                    null,
                    null);
        }

        if (!PROPERTY_ID_PATTERN.matcher(propertyId).matches()) {
            return emptySummary(
                    SiteDataStatus.ACCESS_ERROR,
                    propertyId,
                    null);
        }

        try {
            SiteAnalyticsPort.AnalyticsSummary summary = siteAnalyticsPort.fetchSummary(
                    propertyId,
                    SiteAnalyticsPort.AnalyticsRange.LAST_7_DAYS);

            if (!hasAnyMetricValue(summary.currentMetrics())
                    && !summary.activeUsersTrend().stream().anyMatch(point -> point.value() > 0)) {
                return emptySummary(
                        SiteDataStatus.NO_DATA,
                        propertyId,
                        toLocalDateTime(summary.fetchedAt()));
            }

            return new SiteAnalyticsSummaryAppDto(
                    SiteDataStatus.READY,
                    propertyId,
                    RANGE_LAST_7_DAYS,
                    KPI_ORDER.stream()
                            .map(metric -> new SiteAnalyticsSummaryAppDto.MetricCardAppDto(
                                    metric,
                                    valueOf(summary.currentMetrics(), metric),
                                    summary.previousMetrics().containsKey(metric)
                                            ? summary.previousMetrics().get(metric)
                                            : null,
                                    calculateDeltaPercentage(
                                            valueOf(summary.currentMetrics(), metric),
                                            summary.previousMetrics().get(metric)),
                                    determineDeltaDirection(
                                            valueOf(summary.currentMetrics(), metric),
                                            summary.previousMetrics().get(metric))))
                            .toList(),
                    summary.activeUsersTrend().stream()
                            .map(point -> new SiteAnalyticsSummaryAppDto.TrendPointAppDto(
                                    point.date().toString(),
                                    point.value()))
                            .toList(),
                    toLocalDateTime(summary.fetchedAt()));
        } catch (Exception ex) {
            log.warn("Failed to fetch site analytics summary for property {}", propertyId, ex);
            return emptySummary(
                    SiteDataStatus.ACCESS_ERROR,
                    propertyId,
                    null);
        }
    }

    private boolean isTenantAnalyticsEnabled() {
        Long tenantId = parseTenantId();
        String tenantDbName = tenantContextPort.getTenantDbName();
        if (tenantId == null || !StringUtils.hasText(tenantDbName)) {
            log.warn("Tenant context is not available while resolving GA4 analytics enabled flag");
            return false;
        }

        return configPropertyService.getBoolean(
                tenantId,
                tenantDbName,
                KEY_GA4_ENABLED,
                false);
    }

    private String resolvePropertyId() {
        Long tenantId = parseTenantId();
        String tenantDbName = tenantContextPort.getTenantDbName();
        if (tenantId == null || !StringUtils.hasText(tenantDbName)) {
            log.warn("Tenant context is not available while resolving GA4 property ID");
            return null;
        }

        return configPropertyService.findRaw(
                        tenantId,
                        tenantDbName,
                        KEY_GA4_PROPERTY_ID)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .orElse(null);
    }

    private Long parseTenantId() {
        String tenantId = tenantContextPort.getTenantId();
        if (!StringUtils.hasText(tenantId)) {
            return null;
        }
        try {
            return Long.parseLong(tenantId);
        } catch (NumberFormatException ex) {
            log.warn("Invalid tenant context id {}", tenantId, ex);
            return null;
        }
    }

    private SiteAnalyticsSummaryAppDto emptySummary(
            String status,
            String propertyId,
            LocalDateTime lastSyncedAt
    ) {
        return new SiteAnalyticsSummaryAppDto(
                status,
                propertyId,
                RANGE_LAST_7_DAYS,
                List.of(),
                List.of(),
                lastSyncedAt);
    }

    private boolean hasAnyMetricValue(Map<String, Double> metrics) {
        return metrics.values().stream().anyMatch(value -> value != null && value > 0);
    }

    private double valueOf(Map<String, Double> metrics, String key) {
        return Optional.ofNullable(metrics.get(key)).orElse(0D);
    }

    private Double calculateDeltaPercentage(double currentValue, Double previousValue) {
        if (previousValue == null) {
            return null;
        }
        if (Math.abs(previousValue) < 0.0001D) {
            return currentValue <= 0 ? 0D : null;
        }

        return ((currentValue - previousValue) / previousValue) * 100D;
    }

    private String determineDeltaDirection(double currentValue, Double previousValue) {
        if (previousValue == null) {
            return "unknown";
        }

        double delta = currentValue - previousValue;
        if (Math.abs(delta) < 0.0001D) {
            return "flat";
        }
        return delta > 0 ? "up" : "down";
    }

    private LocalDateTime toLocalDateTime(java.time.Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, java.time.ZoneOffset.UTC);
    }
}
