package com.backend.infrastructure.performance;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.backend.domain.port.SitePerformanceInsightsPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CruxHistoryPortAdapter implements SitePerformanceInsightsPort {

    private final CruxHistoryProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final Map<String, CachedHistory> cache = new ConcurrentHashMap<>();

    @Override
    public Optional<PerformanceHistory> fetchHistory(
            String target,
            TargetScope targetScope,
            String formFactor,
            List<String> metrics,
            int collectionPeriodCount
    ) {
        if (!StringUtils.hasText(target)) {
            return Optional.empty();
        }
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new IllegalStateException("CrUX API key is not configured");
        }

        String cacheKey = String.join(
                ":",
                targetScope.name(),
                target,
                formFactor,
                String.join(",", metrics),
                String.valueOf(collectionPeriodCount));
        CachedHistory cached = cache.get(cacheKey);
        Instant now = Instant.now();
        if (cached != null && cached.expiresAt().isAfter(now)) {
            return cached.result();
        }

        Optional<PerformanceHistory> history = loadHistory(
                target,
                targetScope,
                formFactor,
                metrics,
                collectionPeriodCount);
        cache.put(cacheKey, new CachedHistory(
                history,
                now.plusSeconds(Math.max(properties.getCacheTtlSeconds(), 30))));
        return history;
    }

    private Optional<PerformanceHistory> loadHistory(
            String target,
            TargetScope targetScope,
            String formFactor,
            List<String> metrics,
            int collectionPeriodCount
    ) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("formFactor", formFactor);
        body.put("collectionPeriodCount", collectionPeriodCount);
        ArrayNode metricsNode = body.putArray("metrics");
        metrics.forEach(metricsNode::add);
        if (targetScope == TargetScope.URL) {
            body.put("url", target);
        } else {
            body.put("origin", target);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = properties.getApiBaseUrl().replaceAll("/+$", "")
                + "?key=" + URLEncoder.encode(properties.getApiKey(), StandardCharsets.UTF_8);

        try {
            String response = restTemplate.postForObject(
                    url,
                    new HttpEntity<>(body.toString(), headers),
                    String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode record = root.path("record");
            if (record.isMissingNode() || record.isNull()) {
                return Optional.empty();
            }

            List<CollectionPeriod> periods = parseCollectionPeriods(record.path("collectionPeriods"));
            Map<String, List<Double>> metricSeries = parseMetricSeries(record.path("metrics"), metrics);
            if (periods.isEmpty() || metricSeries.values().stream().allMatch(List::isEmpty)) {
                return Optional.empty();
            }

            return Optional.of(new PerformanceHistory(
                    targetScope,
                    target,
                    formFactor,
                    metricSeries,
                    periods,
                    Instant.now()));
        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to fetch CrUX history", ex);
        }
    }

    private List<CollectionPeriod> parseCollectionPeriods(JsonNode periodsNode) {
        if (!periodsNode.isArray() || periodsNode.isEmpty()) {
            return List.of();
        }

        List<CollectionPeriod> periods = new ArrayList<>();
        for (JsonNode node : periodsNode) {
            periods.add(new CollectionPeriod(
                    parseDate(node.path("firstDate")),
                    parseDate(node.path("lastDate"))));
        }
        return periods;
    }

    private Map<String, List<Double>> parseMetricSeries(JsonNode metricsNode, List<String> metrics) {
        Map<String, List<Double>> metricSeries = new LinkedHashMap<>();
        metrics.forEach(metric -> metricSeries.put(metric, List.of()));

        if (!metricsNode.isObject()) {
            return metricSeries;
        }

        for (String metric : metrics) {
            JsonNode p75Series = metricsNode.path(metric).path("percentilesTimeseries").path("p75s");
            if (!p75Series.isArray()) {
                continue;
            }
            List<Double> values = new ArrayList<>();
            for (JsonNode valueNode : p75Series) {
                values.add(valueNode.isNull() ? null : valueNode.asDouble());
            }
            metricSeries.put(metric, values);
        }

        return metricSeries;
    }

    private LocalDate parseDate(JsonNode dateNode) {
        if (dateNode.isMissingNode() || dateNode.isNull()) {
            return null;
        }
        return LocalDate.of(
                dateNode.path("year").asInt(),
                dateNode.path("month").asInt(),
                dateNode.path("day").asInt());
    }

    private record CachedHistory(
            Optional<PerformanceHistory> result,
            Instant expiresAt
    ) {
    }
}
