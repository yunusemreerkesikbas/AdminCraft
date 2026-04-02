package com.backend.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record SiteAnalyticsSummaryResponse(
        String status,
        String propertyId,
        String range,
        List<MetricCardResponse> cards,
        List<TrendPointResponse> trend,
        LocalDateTime lastSyncedAt
) {

    public record MetricCardResponse(
            String metric,
            double value,
            Double previousValue,
            Double deltaPercentage,
            String deltaDirection
    ) {
    }

    public record TrendPointResponse(
            String date,
            double value
    ) {
    }
}
