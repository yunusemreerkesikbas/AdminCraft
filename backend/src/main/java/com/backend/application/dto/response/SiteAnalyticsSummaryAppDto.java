package com.backend.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record SiteAnalyticsSummaryAppDto(
        String status,
        String propertyId,
        String range,
        List<MetricCardAppDto> cards,
        List<TrendPointAppDto> trend,
        LocalDateTime lastSyncedAt) {

    public record MetricCardAppDto(
            String metric,
            double value,
            Double previousValue,
            Double deltaPercentage,
            String deltaDirection) {
    }

    public record TrendPointAppDto(
            String date,
            double value) {
    }
}
