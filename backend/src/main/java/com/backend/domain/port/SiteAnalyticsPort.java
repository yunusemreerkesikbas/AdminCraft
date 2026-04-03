package com.backend.domain.port;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface SiteAnalyticsPort {

    AnalyticsSummary fetchSummary(String propertyId, AnalyticsRange range);

    enum AnalyticsRange {
        LAST_7_DAYS
    }

    record AnalyticsSummary(
            Map<String, Double> currentMetrics,
            Map<String, Double> previousMetrics,
            List<TrendPoint> activeUsersTrend,
            Instant fetchedAt
    ) {
    }

    record TrendPoint(
            LocalDate date,
            double value
    ) {
    }
}
