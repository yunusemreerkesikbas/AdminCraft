package com.backend.infrastructure.seo;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.backend.domain.port.SiteSeoInsightsPort;
import com.backend.infrastructure.google.GoogleServiceAccountAccessTokenProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleSearchConsolePortAdapter implements SiteSeoInsightsPort {

    private final SearchConsoleProperties properties;
    private final GoogleServiceAccountAccessTokenProvider accessTokenProvider;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final Map<String, CachedSearchPerformance> performanceCache = new ConcurrentHashMap<>();
    private final Map<String, CachedInspection> inspectionCache = new ConcurrentHashMap<>();

    @Override
    public SearchPerformanceSummary fetchSearchPerformance(String propertyUrl, SearchRange range) {
        String cacheKey = propertyUrl + ":" + range.name();
        CachedSearchPerformance cached = performanceCache.get(cacheKey);
        Instant now = Instant.now();
        if (cached != null) {
            if (cached.expiresAt().isAfter(now)) {
                return cached.summary();
            }
            performanceCache.remove(cacheKey, cached);
        }

        SearchPerformanceSummary summary = loadSearchPerformance(propertyUrl);
        performanceCache.put(
                cacheKey,
                new CachedSearchPerformance(
                        summary,
                        now.plusSeconds(Math.max(properties.getCacheTtlSeconds(), 30))));
        return summary;
    }

    @Override
    public UrlInspectionSummary inspectUrl(String propertyUrl, String inspectionUrl) {
        String cacheKey = propertyUrl + ":" + inspectionUrl;
        CachedInspection cached = inspectionCache.get(cacheKey);
        Instant now = Instant.now();
        if (cached != null) {
            if (cached.expiresAt().isAfter(now)) {
                return cached.summary();
            }
            inspectionCache.remove(cacheKey, cached);
        }

        UrlInspectionSummary summary = loadInspection(propertyUrl, inspectionUrl);
        inspectionCache.put(
                cacheKey,
                new CachedInspection(
                        summary,
                        now.plusSeconds(Math.max(properties.getCacheTtlSeconds(), 30))));
        return summary;
    }

    private SearchPerformanceSummary loadSearchPerformance(String propertyUrl) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate currentStart = today.minusDays(27);
        LocalDate previousEnd = currentStart.minusDays(1);
        LocalDate previousStart = previousEnd.minusDays(27);

        Map<String, Double> currentMetrics = executeAggregateQuery(propertyUrl, currentStart, today);
        Map<String, Double> previousMetrics = executeAggregateQuery(propertyUrl, previousStart, previousEnd);
        List<SearchTrendPoint> trend = executeTrendQuery(propertyUrl, currentStart, today);

        return new SearchPerformanceSummary(currentMetrics, previousMetrics, trend, Instant.now());
    }

    private Map<String, Double> executeAggregateQuery(
            String propertyUrl,
            LocalDate startDate,
            LocalDate endDate
    ) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("startDate", startDate.toString());
        body.put("endDate", endDate.toString());
        body.put("rowLimit", 1);

        JsonNode response = executeSearchAnalyticsQuery(propertyUrl, body);
        JsonNode row = response.path("rows").isArray() && !response.path("rows").isEmpty()
                ? response.path("rows").get(0)
                : objectMapper.createObjectNode();

        Map<String, Double> metrics = new LinkedHashMap<>();
        metrics.put("clicks", row.path("clicks").asDouble(0D));
        metrics.put("impressions", row.path("impressions").asDouble(0D));
        metrics.put("ctr", row.path("ctr").asDouble(0D));
        metrics.put("position", row.path("position").asDouble(0D));
        return metrics;
    }

    private List<SearchTrendPoint> executeTrendQuery(
            String propertyUrl,
            LocalDate startDate,
            LocalDate endDate
    ) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("startDate", startDate.toString());
        body.put("endDate", endDate.toString());
        body.put("rowLimit", 50);
        body.putArray("dimensions").add("date");

        JsonNode response = executeSearchAnalyticsQuery(propertyUrl, body);
        JsonNode rows = response.path("rows");
        if (!rows.isArray() || rows.isEmpty()) {
            return List.of();
        }

        List<SearchTrendPoint> trend = new ArrayList<>();
        for (JsonNode row : rows) {
            JsonNode keys = row.path("keys");
            String date = keys.isArray() && !keys.isEmpty() ? keys.get(0).asText(null) : null;
            if (date == null) {
                continue;
            }
            trend.add(new SearchTrendPoint(
                    LocalDate.parse(date),
                    row.path("clicks").asDouble(0D),
                    row.path("impressions").asDouble(0D)));
        }

        return trend.stream()
                .sorted(Comparator.comparing(SearchTrendPoint::date))
                .toList();
    }

    private JsonNode executeSearchAnalyticsQuery(String propertyUrl, ObjectNode body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessTokenProvider.getAccessToken(properties.getScope()));
        headers.setContentType(MediaType.APPLICATION_JSON);

        String encodedPropertyUrl = URLEncoder.encode(propertyUrl, StandardCharsets.UTF_8);
        URI uri = URI.create(properties.getSearchAnalyticsBaseUrl().replaceAll("/+$", "")
                + "/sites/" + encodedPropertyUrl + "/searchAnalytics/query");

        ResponseEntity<String> response = restTemplate.postForEntity(
                uri,
                new HttpEntity<>(body.toString(), headers),
                String.class);
        try {
            return objectMapper.readTree(response.getBody());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse Search Console analytics response", ex);
        }
    }

    private UrlInspectionSummary loadInspection(String propertyUrl, String inspectionUrl) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("inspectionUrl", inspectionUrl);
        body.put("siteUrl", propertyUrl);
        body.put("languageCode", properties.getLanguageCode());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessTokenProvider.getAccessToken(properties.getScope()));
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = properties.getApiBaseUrl().replaceAll("/+$", "")
                + "/urlInspection/index:inspect";

        ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(body.toString(), headers),
                String.class);
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode indexStatus = root.path("inspectionResult").path("indexStatusResult");

            List<String> sitemaps = new ArrayList<>();
            JsonNode sitemapsNode = indexStatus.path("referringSitemaps");
            if (sitemapsNode.isArray()) {
                sitemapsNode.forEach(item -> sitemaps.add(item.asText()));
            }

            return new UrlInspectionSummary(
                    textOrNull(indexStatus.path("verdict")),
                    textOrNull(indexStatus.path("coverageState")),
                    textOrNull(indexStatus.path("robotsTxtState")),
                    textOrNull(indexStatus.path("indexingState")),
                    textOrNull(indexStatus.path("pageFetchState")),
                    parseInstant(indexStatus.path("lastCrawlTime")),
                    textOrNull(indexStatus.path("googleCanonical")),
                    textOrNull(indexStatus.path("userCanonical")),
                    sitemaps,
                    Instant.now());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse Search Console inspection response", ex);
        }
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asText(null);
        return value == null || value.isBlank() ? null : value;
    }

    private Instant parseInstant(JsonNode node) {
        String value = textOrNull(node);
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (Exception ex) {
            log.debug("Unable to parse Search Console timestamp {}", value, ex);
            return null;
        }
    }

    private record CachedSearchPerformance(
            SearchPerformanceSummary summary,
            Instant expiresAt
    ) {
    }

    private record CachedInspection(
            UrlInspectionSummary summary,
            Instant expiresAt
    ) {
    }
}
