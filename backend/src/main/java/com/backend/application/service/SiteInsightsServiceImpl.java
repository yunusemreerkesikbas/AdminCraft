package com.backend.application.service;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.backend.application.dto.response.SiteInsightsSummaryAppDto;
import com.backend.application.service.config.ConfigPropertyService;
import com.backend.application.service.config.GlobalRuntimeConfigService;
import com.backend.domain.entity.Tenant;
import com.backend.domain.port.FrontendConfigPort;
import com.backend.domain.port.SitePerformanceInsightsPort;
import com.backend.domain.port.SiteSeoInsightsPort;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.TenantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SiteInsightsServiceImpl implements SiteInsightsService {

    static final String STATUS_READY = "READY";
    static final String STATUS_NOT_CONFIGURED = "NOT_CONFIGURED";
    static final String STATUS_DISABLED = "DISABLED";
    static final String STATUS_ACCESS_ERROR = "ACCESS_ERROR";
    static final String STATUS_NO_DATA = "NO_DATA";

    static final String SEO_RANGE_LAST_28_DAYS = "LAST_28_DAYS";
    static final String KEY_SEO_INSIGHTS_ENABLED = "seo.insights.enabled";
    static final String KEY_SEARCH_CONSOLE_PROPERTY_URL = "seo.search_console.property_url";

    private static final String FORM_FACTOR_DESKTOP = "DESKTOP";
    private static final List<String> SEO_METRIC_ORDER = List.of("clicks", "impressions", "ctr", "position");
    private static final List<String> PERFORMANCE_METRICS = List.of(
            "largest_contentful_paint",
            "interaction_to_next_paint",
            "cumulative_layout_shift",
            "experimental_time_to_first_byte");

    private final ConfigPropertyService configPropertyService;
    private final GlobalRuntimeConfigService globalRuntimeConfigService;
    private final TenantContextPort tenantContextPort;
    private final TenantRepository tenantRepository;
    private final FrontendConfigPort frontendConfigPort;
    private final SiteSeoInsightsPort siteSeoInsightsPort;
    private final SitePerformanceInsightsPort sitePerformanceInsightsPort;

    @Override
    public SiteInsightsSummaryAppDto getSummary() {
        if (!Boolean.TRUE.equals(globalRuntimeConfigService.getSeoInsightsEnabled())) {
            return emptySummary(STATUS_DISABLED, null, null);
        }

        if (!isTenantInsightsEnabled()) {
            return emptySummary(STATUS_DISABLED, null, null);
        }

        String resolvedUrl = resolvePublicUrl();
        String resolvedOrigin = resolveOrigin(resolvedUrl);

        SiteInsightsSummaryAppDto.SeoAppDto seo = buildSeoSummary(resolvedUrl);
        SiteInsightsSummaryAppDto.PerformanceAppDto performance = buildPerformanceSummary(resolvedUrl, resolvedOrigin);

        return new SiteInsightsSummaryAppDto(
                resolvedUrl,
                resolvedOrigin,
                maxTimestamp(seo.lastSyncedAt(), performance.lastSyncedAt()),
                seo,
                performance);
    }

    private SiteInsightsSummaryAppDto emptySummary(
            String status,
            String resolvedUrl,
            String resolvedOrigin
    ) {
        return new SiteInsightsSummaryAppDto(
                resolvedUrl,
                resolvedOrigin,
                null,
                emptySeo(status, null, null, null),
                emptyPerformance(status, null, null, null, null));
    }

    private SiteInsightsSummaryAppDto.SeoAppDto buildSeoSummary(String resolvedUrl) {
        String propertyUrl = resolveSearchConsolePropertyUrl();
        if (!StringUtils.hasText(propertyUrl)) {
            return emptySeo(STATUS_NOT_CONFIGURED, null, null, null);
        }
        if (!isValidSearchConsolePropertyUrl(propertyUrl)) {
            return emptySeo(STATUS_ACCESS_ERROR, propertyUrl, null, null);
        }

        SiteSeoInsightsPort.SearchPerformanceSummary searchPerformance = null;
        SiteSeoInsightsPort.UrlInspectionSummary inspection = null;
        boolean searchFailed = false;
        boolean inspectionFailed = false;

        try {
            searchPerformance = siteSeoInsightsPort.fetchSearchPerformance(
                    propertyUrl,
                    SiteSeoInsightsPort.SearchRange.LAST_28_DAYS);
        } catch (Exception ex) {
            searchFailed = true;
            log.warn("Failed to fetch Search Console performance for property {}", propertyUrl, ex);
        }

        if (StringUtils.hasText(resolvedUrl)) {
            try {
                inspection = siteSeoInsightsPort.inspectUrl(propertyUrl, resolvedUrl);
            } catch (Exception ex) {
                inspectionFailed = true;
                log.warn("Failed to inspect Search Console URL {} for property {}", resolvedUrl, propertyUrl, ex);
            }
        }

        if (searchFailed && (inspection == null || inspectionFailed)) {
            return emptySeo(STATUS_ACCESS_ERROR, propertyUrl, null, null);
        }

        SiteSeoInsightsPort.SearchPerformanceSummary searchSummary = searchPerformance;

        List<SiteInsightsSummaryAppDto.MetricCardAppDto> cards = searchSummary == null
                ? List.of()
                : SEO_METRIC_ORDER.stream()
                        .map(metric -> new SiteInsightsSummaryAppDto.MetricCardAppDto(
                                metric,
                                metricValue(searchSummary.currentMetrics(), metric),
                                searchSummary.previousMetrics().get(metric),
                                calculateDeltaPercentage(
                                        metricValue(searchSummary.currentMetrics(), metric),
                                        searchSummary.previousMetrics().get(metric)),
                                determineDeltaDirection(
                                        metricValue(searchSummary.currentMetrics(), metric),
                                        searchSummary.previousMetrics().get(metric))))
                        .toList();

        List<SiteInsightsSummaryAppDto.SeoTrendPointAppDto> trend = searchSummary == null
                ? List.of()
                : searchSummary.trend().stream()
                        .map(point -> new SiteInsightsSummaryAppDto.SeoTrendPointAppDto(
                                point.date().toString(),
                                point.clicks(),
                                point.impressions()))
                        .toList();

        SiteInsightsSummaryAppDto.InspectionAppDto inspectionDto = inspection == null
                ? null
                : new SiteInsightsSummaryAppDto.InspectionAppDto(
                        inspection.verdict(),
                        inspection.coverageState(),
                        inspection.robotsTxtState(),
                        inspection.indexingState(),
                        inspection.pageFetchState(),
                        toLocalDateTime(inspection.lastCrawlTime()),
                        inspection.googleCanonical(),
                        inspection.userCanonical(),
                        inspection.sitemaps());

        boolean hasSearchData = !cards.isEmpty() || !trend.isEmpty();
        boolean hasInspectionData = inspectionDto != null;
        String status = hasSearchData || hasInspectionData ? STATUS_READY : STATUS_NO_DATA;

        return new SiteInsightsSummaryAppDto.SeoAppDto(
                status,
                propertyUrl,
                SEO_RANGE_LAST_28_DAYS,
                cards,
                trend,
                inspectionDto,
                maxTimestamp(
                        toLocalDateTime(searchSummary != null ? searchSummary.fetchedAt() : null),
                        toLocalDateTime(inspection != null ? inspection.fetchedAt() : null)));
    }

    private SiteInsightsSummaryAppDto.PerformanceAppDto buildPerformanceSummary(
            String resolvedUrl,
            String resolvedOrigin
    ) {
        if (!StringUtils.hasText(resolvedUrl) || !StringUtils.hasText(resolvedOrigin)) {
            return emptyPerformance(STATUS_NOT_CONFIGURED, null, null, null, null);
        }

        Optional<SitePerformanceInsightsPort.PerformanceHistory> urlHistory;
        try {
            urlHistory = sitePerformanceInsightsPort.fetchHistory(
                    resolvedUrl,
                    SitePerformanceInsightsPort.TargetScope.URL,
                    FORM_FACTOR_DESKTOP,
                    PERFORMANCE_METRICS,
                    6);
        } catch (Exception ex) {
            log.warn("Failed to fetch CrUX URL history for {}", resolvedUrl, ex);
            return emptyPerformance(STATUS_ACCESS_ERROR, null, null, null, null);
        }

        SitePerformanceInsightsPort.PerformanceHistory history = urlHistory.orElseGet(() -> {
            try {
                return sitePerformanceInsightsPort.fetchHistory(
                                resolvedOrigin,
                                SitePerformanceInsightsPort.TargetScope.ORIGIN,
                                FORM_FACTOR_DESKTOP,
                                PERFORMANCE_METRICS,
                                6)
                        .orElse(null);
            } catch (Exception ex) {
                log.warn("Failed to fetch CrUX origin history for {}", resolvedOrigin, ex);
                return null;
            }
        });

        if (history == null) {
            return emptyPerformance(STATUS_NO_DATA, null, null, null, null);
        }

        Double lcp = latestMetric(history, "largest_contentful_paint");
        Double inp = latestMetric(history, "interaction_to_next_paint");
        Double cls = latestMetric(history, "cumulative_layout_shift");
        Double ttfb = latestMetric(history, "experimental_time_to_first_byte");

        List<SiteInsightsSummaryAppDto.PerformanceMetricAppDto> metrics = List.of(
                performanceMetric("lcp", lcp),
                performanceMetric("inp", inp),
                performanceMetric("cls", cls),
                performanceMetric("ttfb", ttfb));

        List<Integer> metricScores = metrics.stream()
                .map(metric -> scoreForAssessment(metric.assessment()))
                .filter(score -> score >= 0)
                .toList();
        int scoreValue = metricScores.isEmpty()
                ? 0
                : Math.round((float) metricScores.stream().mapToInt(Integer::intValue).sum() / metricScores.size());

        String scoreLabel = resolveScoreLabel(metrics);
        SiteInsightsSummaryAppDto.ScoreAppDto score = new SiteInsightsSummaryAppDto.ScoreAppDto(scoreValue, scoreLabel);

        List<SiteInsightsSummaryAppDto.PerformanceTrendPointAppDto> trend = buildPerformanceTrend(history);

        return new SiteInsightsSummaryAppDto.PerformanceAppDto(
                STATUS_READY,
                history.targetScope().name(),
                history.target(),
                history.formFactor(),
                score,
                metrics,
                trend,
                toLocalDateTime(history.fetchedAt()));
    }

    private SiteInsightsSummaryAppDto.SeoAppDto emptySeo(
            String status,
            String propertyUrl,
            SiteInsightsSummaryAppDto.InspectionAppDto inspection,
            LocalDateTime lastSyncedAt
    ) {
        return new SiteInsightsSummaryAppDto.SeoAppDto(
                status,
                propertyUrl,
                SEO_RANGE_LAST_28_DAYS,
                List.of(),
                List.of(),
                inspection,
                lastSyncedAt);
    }

    private SiteInsightsSummaryAppDto.PerformanceAppDto emptyPerformance(
            String status,
            String targetScope,
            String target,
            SiteInsightsSummaryAppDto.ScoreAppDto score,
            LocalDateTime lastSyncedAt
    ) {
        return new SiteInsightsSummaryAppDto.PerformanceAppDto(
                status,
                targetScope,
                target,
                FORM_FACTOR_DESKTOP,
                score,
                List.of(),
                List.of(),
                lastSyncedAt);
    }

    private boolean isTenantInsightsEnabled() {
        Long tenantId = currentTenantId();
        String tenantDbName = tenantContextPort.getTenantDbName();
        if (tenantId == null || !StringUtils.hasText(tenantDbName)) {
            log.warn("Tenant context is not available while resolving SEO insights flag");
            return false;
        }

        return configPropertyService.getBoolean(
                tenantId,
                tenantDbName,
                KEY_SEO_INSIGHTS_ENABLED,
                false);
    }

    private String resolveSearchConsolePropertyUrl() {
        Long tenantId = currentTenantId();
        String tenantDbName = tenantContextPort.getTenantDbName();
        if (tenantId == null || !StringUtils.hasText(tenantDbName)) {
            return null;
        }

        return configPropertyService.findRaw(
                        tenantId,
                        tenantDbName,
                        KEY_SEARCH_CONSOLE_PROPERTY_URL)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .orElse(null);
    }

    private String resolvePublicUrl() {
        Tenant tenant = resolveCurrentTenant();
        if (tenant == null) {
            return null;
        }

        if (StringUtils.hasText(tenant.getCustomDomain())) {
            return normalizeAbsoluteUrl(tenant.getCustomDomain().trim(), "https");
        }

        String baseUrl = frontendConfigPort.getBaseUrl();
        if (!StringUtils.hasText(baseUrl) || !StringUtils.hasText(tenant.getSubdomain())) {
            return null;
        }

        String candidate = baseUrl.contains("%s")
                ? String.format(baseUrl, tenant.getSubdomain()).trim()
                : baseUrl.trim();
        String defaultScheme = candidate.toLowerCase(Locale.ROOT).startsWith("http://") ? "http" : "https";
        return normalizeAbsoluteUrl(candidate, defaultScheme);
    }

    private Tenant resolveCurrentTenant() {
        Long tenantId = currentTenantId();
        if (tenantId == null) {
            return null;
        }

        return withPlatformContext(() -> tenantRepository.findById(tenantId).orElse(null));
    }

    private <T> T withPlatformContext(java.util.function.Supplier<T> supplier) {
        String savedTenantId = tenantContextPort.getTenantId();
        String savedTenantDbName = tenantContextPort.getTenantDbName();
        String savedSubdomain = tenantContextPort.getSubdomain();

        try {
            tenantContextPort.clear();
            return supplier.get();
        } finally {
            tenantContextPort.clear();
            if (savedTenantId != null) {
                tenantContextPort.setTenantId(savedTenantId);
            }
            if (savedTenantDbName != null) {
                tenantContextPort.setTenantDbName(savedTenantDbName);
            }
            if (savedSubdomain != null) {
                tenantContextPort.setSubdomain(savedSubdomain);
            }
        }
    }

    private String normalizeAbsoluteUrl(String value, String defaultScheme) {
        String candidate = value.trim();
        if (!candidate.contains("://")) {
            candidate = defaultScheme + "://" + candidate;
        }
        try {
            URI uri = URI.create(candidate);
            if (!StringUtils.hasText(uri.getScheme()) || !StringUtils.hasText(uri.getHost())) {
                return null;
            }
            String normalizedPath = uri.getPath() == null || uri.getPath().isBlank() ? "" : uri.getPath();
            return new URI(
                    uri.getScheme().toLowerCase(Locale.ROOT),
                    uri.getUserInfo(),
                    uri.getHost().toLowerCase(Locale.ROOT),
                    uri.getPort(),
                    normalizedPath,
                    null,
                    null).toString().replaceAll("/+$", "");
        } catch (Exception ex) {
            log.warn("Unable to normalize public URL {}", value, ex);
            return null;
        }
    }

    private String resolveOrigin(String resolvedUrl) {
        if (!StringUtils.hasText(resolvedUrl)) {
            return null;
        }
        try {
            URI uri = URI.create(resolvedUrl);
            if (!StringUtils.hasText(uri.getScheme()) || !StringUtils.hasText(uri.getHost())) {
                return null;
            }
            return new URI(
                    uri.getScheme().toLowerCase(Locale.ROOT),
                    null,
                    uri.getHost().toLowerCase(Locale.ROOT),
                    uri.getPort(),
                    null,
                    null,
                    null).toString();
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean isValidSearchConsolePropertyUrl(String propertyUrl) {
        String normalized = propertyUrl.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("sc-domain:") || normalized.startsWith("http://") || normalized.startsWith("https://");
    }

    private Long currentTenantId() {
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

    private double metricValue(Map<String, Double> metrics, String key) {
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

    private Double latestMetric(
            SitePerformanceInsightsPort.PerformanceHistory history,
            String metric
    ) {
        List<Double> values = history.metricSeries().getOrDefault(metric, List.of());
        for (int index = values.size() - 1; index >= 0; index--) {
            Double value = values.get(index);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private List<SiteInsightsSummaryAppDto.PerformanceTrendPointAppDto> buildPerformanceTrend(
            SitePerformanceInsightsPort.PerformanceHistory history
    ) {
        List<SiteInsightsSummaryAppDto.PerformanceTrendPointAppDto> trend = new ArrayList<>();
        List<SitePerformanceInsightsPort.CollectionPeriod> periods = history.collectionPeriods();

        for (int index = 0; index < periods.size(); index++) {
            SitePerformanceInsightsPort.CollectionPeriod period = periods.get(index);
            trend.add(new SiteInsightsSummaryAppDto.PerformanceTrendPointAppDto(
                    period.startDate() != null ? period.startDate().toString() : null,
                    period.endDate() != null ? period.endDate().toString() : null,
                    valueAt(history.metricSeries().get("largest_contentful_paint"), index),
                    valueAt(history.metricSeries().get("interaction_to_next_paint"), index),
                    valueAt(history.metricSeries().get("cumulative_layout_shift"), index),
                    valueAt(history.metricSeries().get("experimental_time_to_first_byte"), index)));
        }

        return trend.stream()
                .sorted(Comparator.comparing(SiteInsightsSummaryAppDto.PerformanceTrendPointAppDto::endDate))
                .toList();
    }

    private Double valueAt(List<Double> values, int index) {
        if (values == null || index < 0 || index >= values.size()) {
            return null;
        }
        return values.get(index);
    }

    private SiteInsightsSummaryAppDto.PerformanceMetricAppDto performanceMetric(String metric, Double value) {
        String assessment = performanceAssessment(metric, value);
        return new SiteInsightsSummaryAppDto.PerformanceMetricAppDto(
                metric,
                value,
                formatPerformanceMetric(metric, value),
                assessment);
    }

    private String performanceAssessment(String metric, Double value) {
        if (value == null) {
            return "UNKNOWN";
        }

        return switch (metric) {
            case "lcp" -> value <= 2500D ? "GOOD" : value <= 4000D ? "NEEDS_IMPROVEMENT" : "POOR";
            case "inp" -> value <= 200D ? "GOOD" : value <= 500D ? "NEEDS_IMPROVEMENT" : "POOR";
            case "cls" -> value <= 0.1D ? "GOOD" : value <= 0.25D ? "NEEDS_IMPROVEMENT" : "POOR";
            case "ttfb" -> value <= 800D ? "GOOD" : value <= 1800D ? "NEEDS_IMPROVEMENT" : "POOR";
            default -> "UNKNOWN";
        };
    }

    private String formatPerformanceMetric(String metric, Double value) {
        if (value == null) {
            return null;
        }

        return switch (metric) {
            case "cls" -> String.format(Locale.US, "%.2f", value);
            case "lcp", "inp", "ttfb" -> Math.round(value) + " ms";
            default -> String.valueOf(value);
        };
    }

    private int scoreForAssessment(String assessment) {
        return switch (assessment) {
            case "GOOD" -> 100;
            case "NEEDS_IMPROVEMENT" -> 60;
            case "POOR" -> 20;
            default -> -1;
        };
    }

    private String resolveScoreLabel(List<SiteInsightsSummaryAppDto.PerformanceMetricAppDto> metrics) {
        boolean hasPoor = metrics.stream().anyMatch(metric -> "POOR".equals(metric.assessment()));
        if (hasPoor) {
            return "CRITICAL";
        }

        boolean hasNeedsImprovement = metrics.stream()
                .anyMatch(metric -> "NEEDS_IMPROVEMENT".equals(metric.assessment()));
        if (hasNeedsImprovement) {
            return "ATTENTION";
        }

        boolean hasGood = metrics.stream().anyMatch(metric -> "GOOD".equals(metric.assessment()));
        return hasGood ? "HEALTHY" : "ATTENTION";
    }

    private LocalDateTime maxTimestamp(LocalDateTime left, LocalDateTime right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.isAfter(right) ? left : right;
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
