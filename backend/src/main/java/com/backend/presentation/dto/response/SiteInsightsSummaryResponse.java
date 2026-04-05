package com.backend.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record SiteInsightsSummaryResponse(
        String resolvedUrl,
        String resolvedOrigin,
        LocalDateTime lastSyncedAt,
        SeoResponse seo,
        PerformanceResponse performance
) {

    public record SeoResponse(
            String status,
            String propertyUrl,
            String range,
            List<MetricCardResponse> cards,
            List<SeoTrendPointResponse> trend,
            InspectionResponse inspection,
            LocalDateTime lastSyncedAt
    ) {
    }

    public record MetricCardResponse(
            String metric,
            double value,
            Double previousValue,
            Double deltaPercentage,
            String deltaDirection
    ) {
    }

    public record SeoTrendPointResponse(
            String date,
            double clicks,
            double impressions
    ) {
    }

    public record InspectionResponse(
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

    public record PerformanceResponse(
            String status,
            String targetScope,
            String target,
            String formFactor,
            ScoreResponse score,
            List<PerformanceMetricResponse> metrics,
            List<PerformanceTrendPointResponse> trend,
            LocalDateTime lastSyncedAt
    ) {
    }

    public record ScoreResponse(
            int value,
            String label
    ) {
    }

    public record PerformanceMetricResponse(
            String metric,
            Double value,
            String displayValue,
            String assessment
    ) {
    }

    public record PerformanceTrendPointResponse(
            String startDate,
            String endDate,
            Double lcp,
            Double inp,
            Double cls,
            Double ttfb
    ) {
    }
}
