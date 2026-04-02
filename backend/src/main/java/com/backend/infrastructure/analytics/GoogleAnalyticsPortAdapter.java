package com.backend.infrastructure.analytics;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.backend.domain.port.SiteAnalyticsPort;
import com.backend.infrastructure.google.GoogleServiceAccountAccessTokenProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleAnalyticsPortAdapter implements SiteAnalyticsPort {

    private final GoogleAnalyticsProperties properties;
    private final GoogleServiceAccountAccessTokenProvider accessTokenProvider;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final Map<String, CachedSummary> summaryCache = new ConcurrentHashMap<>();

    @Override
    public AnalyticsSummary fetchSummary(String propertyId, AnalyticsRange range) {
        String cacheKey = propertyId + ":" + range.name();
        CachedSummary cachedSummary = summaryCache.get(cacheKey);
        Instant now = Instant.now();
        if (cachedSummary != null && cachedSummary.expiresAt().isAfter(now)) {
            return cachedSummary.summary();
        }

        AnalyticsSummary summary = loadSummary(propertyId, range);
        summaryCache.put(
                cacheKey,
                new CachedSummary(
                        summary,
                        now.plusSeconds(Math.max(properties.getCacheTtlSeconds(), 30))));
        return summary;
    }

    private AnalyticsSummary loadSummary(String propertyId, AnalyticsRange range) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate currentStart = today.minusDays(6);
        LocalDate previousEnd = currentStart.minusDays(1);
        LocalDate previousStart = previousEnd.minusDays(6);

        Map<String, Double> currentMetrics = executeAggregateReport(
                propertyId,
                currentStart,
                today,
                List.of("activeUsers", "screenPageViews", "newUsers", "engagementRate"));
        Map<String, Double> previousMetrics = executeAggregateReport(
                propertyId,
                previousStart,
                previousEnd,
                List.of("activeUsers", "screenPageViews", "newUsers", "engagementRate"));
        List<TrendPoint> trend = executeTrendReport(propertyId, currentStart, today);

        return new AnalyticsSummary(currentMetrics, previousMetrics, trend, Instant.now());
    }

    private Map<String, Double> executeAggregateReport(
            String propertyId,
            LocalDate startDate,
            LocalDate endDate,
            List<String> metrics
    ) {
        ObjectNode body = objectMapper.createObjectNode();
        ArrayNode metricsNode = body.putArray("metrics");
        metrics.forEach(metric -> metricsNode.addObject().put("name", metric));
        body.putArray("dateRanges")
                .addObject()
                .put("startDate", startDate.toString())
                .put("endDate", endDate.toString());

        JsonNode response = executeReport(propertyId, body);
        JsonNode rows = response.path("rows");
        if (!rows.isArray() || rows.isEmpty()) {
            return metrics.stream().collect(java.util.stream.Collectors.toMap(
                    metric -> metric,
                    metric -> 0D,
                    (left, right) -> left,
                    java.util.LinkedHashMap::new));
        }

        JsonNode metricValues = rows.get(0).path("metricValues");
        java.util.LinkedHashMap<String, Double> result = new java.util.LinkedHashMap<>();
        for (int i = 0; i < metrics.size(); i++) {
            result.put(metrics.get(i), parseMetricValue(metricValues.get(i)));
        }
        return result;
    }

    private List<TrendPoint> executeTrendReport(
            String propertyId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        ObjectNode body = objectMapper.createObjectNode();
        body.putArray("dimensions").addObject().put("name", "date");
        body.putArray("metrics").addObject().put("name", "activeUsers");
        body.putArray("dateRanges")
                .addObject()
                .put("startDate", startDate.toString())
                .put("endDate", endDate.toString());
        body.putArray("orderBys")
                .addObject()
                .set("dimension", objectMapper.createObjectNode().put("dimensionName", "date"));

        JsonNode response = executeReport(propertyId, body);
        JsonNode rows = response.path("rows");
        if (!rows.isArray() || rows.isEmpty()) {
            return List.of();
        }

        return java.util.stream.StreamSupport.stream(rows.spliterator(), false)
                .map(row -> new TrendPoint(
                        parseDate(row.path("dimensionValues").get(0).path("value").asText()),
                        parseMetricValue(row.path("metricValues").get(0))))
                .sorted(Comparator.comparing(TrendPoint::date))
                .toList();
    }

    private JsonNode executeReport(String propertyId, ObjectNode body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessTokenProvider.getAccessToken(properties.getScope()));
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
        String url = properties.getApiBaseUrl().replaceAll("/+$", "")
                + "/properties/" + propertyId + ":runReport";

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        try {
            return objectMapper.readTree(response.getBody());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse Google Analytics response", ex);
        }
    }

    private double parseMetricValue(JsonNode metricNode) {
        if (metricNode == null || metricNode.isMissingNode()) {
            return 0D;
        }

        String value = metricNode.path("value").asText("0");
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            log.debug("Unable to parse GA4 metric value '{}'", value, ex);
            return 0D;
        }
    }

    private LocalDate parseDate(String gaDate) {
        if (!StringUtils.hasText(gaDate) || gaDate.length() != 8) {
            throw new IllegalStateException("Unexpected GA4 date value: " + gaDate);
        }

        return LocalDate.of(
                Integer.parseInt(gaDate.substring(0, 4)),
                Integer.parseInt(gaDate.substring(4, 6)),
                Integer.parseInt(gaDate.substring(6, 8)));
    }

    private record CachedSummary(
            AnalyticsSummary summary,
            Instant expiresAt
    ) {
    }
}
