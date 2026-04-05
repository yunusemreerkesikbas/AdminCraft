package com.backend.domain.port;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SitePerformanceInsightsPort {

    Optional<PerformanceHistory> fetchHistory(
            String target,
            TargetScope targetScope,
            String formFactor,
            List<String> metrics,
            int collectionPeriodCount
    );

    enum TargetScope {
        URL,
        ORIGIN
    }

    record PerformanceHistory(
            TargetScope targetScope,
            String target,
            String formFactor,
            Map<String, List<Double>> metricSeries,
            List<CollectionPeriod> collectionPeriods,
            Instant fetchedAt
    ) {
    }

    record CollectionPeriod(
            LocalDate startDate,
            LocalDate endDate
    ) {
    }
}
