package com.backend.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record SiteInsightsSummaryAppDto(
        String resolvedUrl,
        String resolvedOrigin,
        LocalDateTime lastSyncedAt,
        SeoAppDto seo,
        PerformanceAppDto performance
) {

    public record SeoAppDto(
            String status,
            String propertyUrl,
            String range,
            List<MetricCardAppDto> cards,
            List<SeoTrendPointAppDto> trend,
            InspectionAppDto inspection,
            LocalDateTime lastSyncedAt
    ) {
    }

    public record MetricCardAppDto(
            String metric,
            double value,
            Double previousValue,
            Double deltaPercentage,
            String deltaDirection
    ) {
    }

    public record SeoTrendPointAppDto(
            String date,
            double clicks,
            double impressions
    ) {
    }

    public record InspectionAppDto(
            String verdict,
            String coverageState,
            String robotsTxtState,
            String indexingState,
            String pageFetchState,
            LocalDateTime lastCrawlTime,
            String googleCanonical,
            String userCanonical,
            List<String> sitemaps
    ) {
    }

    public record PerformanceAppDto(
            String status,
            String targetScope,
            String target,
            String formFactor,
            ScoreAppDto score,
            List<PerformanceMetricAppDto> metrics,
            List<PerformanceTrendPointAppDto> trend,
            LocalDateTime lastSyncedAt
    ) {
    }

    public record ScoreAppDto(
            int value,
            String label
    ) {
    }

    public record PerformanceMetricAppDto(
            String metric,
            Double value,
            String displayValue,
            String assessment
    ) {
    }

    public record PerformanceTrendPointAppDto(
            String startDate,
            String endDate,
            Double lcp,
            Double inp,
            Double cls,
            Double ttfb
    ) {
    }
}
