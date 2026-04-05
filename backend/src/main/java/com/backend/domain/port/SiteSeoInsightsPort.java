package com.backend.domain.port;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface SiteSeoInsightsPort {

    SearchPerformanceSummary fetchSearchPerformance(
            String propertyUrl,
            SearchRange range
    );

    UrlInspectionSummary inspectUrl(
            String propertyUrl,
            String inspectionUrl
    );

    enum SearchRange {
        LAST_28_DAYS
    }

    record SearchPerformanceSummary(
            Map<String, Double> currentMetrics,
            Map<String, Double> previousMetrics,
            List<SearchTrendPoint> trend,
            Instant fetchedAt
    ) {
    }

    record SearchTrendPoint(
            LocalDate date,
            double clicks,
            double impressions
    ) {
    }

    record UrlInspectionSummary(
            String verdict,
            String coverageState,
            String robotsTxtState,
            String indexingState,
            String pageFetchState,
            Instant lastCrawlTime,
            String googleCanonical,
            String userCanonical,
            List<String> sitemaps,
            Instant fetchedAt
    ) {
    }
}
