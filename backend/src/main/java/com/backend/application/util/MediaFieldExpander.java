package com.backend.application.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.backend.application.dto.delivery.ResponsiveMediaDeliveryResponse;
import com.backend.application.service.ResponsiveMediaService;
import com.backend.domain.entity.EntryFieldDefinition;
import com.backend.domain.entity.ResponsiveMediaSet;
import com.backend.domain.enums.EntryFieldType;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.EntryFieldDefinitionRepository;
import com.backend.domain.repository.ResponsiveMediaSetRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for expanding MEDIA type fields in component/entry custom data.
 * Converts media IDs to full ResponsiveMediaDeliveryResponse objects.
 *
 * Uses batch fetching to prevent N+1 query problems.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MediaFieldExpander {

    private final EntryFieldDefinitionRepository entryFieldDefinitionRepository;
    private final ResponsiveMediaSetRepository responsiveMediaSetRepository;
    private final ResponsiveMediaService responsiveMediaService;
    private final ObjectMapper objectMapper;

    /**
     * Expands MEDIA type fields from ID to full ResponsiveMediaDeliveryResponse
     * objects.
     * Uses batch fetching to prevent N+1 queries.
     *
     * @param customFields    The custom fields map from entry i18n
     * @param componentTypeId The component type ID for field definition lookup
     * @param lang            The language for i18n content
     * @return Map with MEDIA fields expanded to full response objects
     */
    public Map<String, Object> expandMediaFields(Map<String, Object> customFields, Long componentTypeId,
            Language lang) {
        if (customFields == null || customFields.isEmpty() || componentTypeId == null) {
            return customFields != null ? customFields : new HashMap<>();
        }

        // Get field definitions to identify MEDIA type fields
        List<EntryFieldDefinition> fieldDefinitions = entryFieldDefinitionRepository
                .findByComponentTypeId(componentTypeId);

        Set<String> mediaFieldKeys = fieldDefinitions.stream()
                .filter(f -> f.getFieldType() == EntryFieldType.MEDIA)
                .map(EntryFieldDefinition::getFieldKey)
                .collect(Collectors.toSet());

        if (mediaFieldKeys.isEmpty()) {
            return customFields;
        }

        // Collect all media IDs first for batch fetching
        Set<Long> mediaIds = mediaFieldKeys.stream()
                .map(customFields::get)
                .filter(value -> value != null)
                .map(this::convertToLong)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (mediaIds.isEmpty()) {
            return customFields;
        }

        // Batch fetch all ResponsiveMediaSets
        Map<Long, ResponsiveMediaSet> mediaSetMap = responsiveMediaSetRepository.findByIdIn(mediaIds)
                .stream()
                .collect(Collectors.toMap(ResponsiveMediaSet::getId, m -> m));

        // Expand fields using the pre-fetched map
        Map<String, Object> expandedFields = new HashMap<>(customFields);

        for (String fieldKey : mediaFieldKeys) {
            Object value = customFields.get(fieldKey);
            if (value == null) {
                continue;
            }

            try {
                Long responsiveMediaId = convertToLong(value);
                if (responsiveMediaId != null) {
                    ResponsiveMediaSet mediaSet = mediaSetMap.get(responsiveMediaId);
                    if (mediaSet != null) {
                        ResponsiveMediaDeliveryResponse expanded = responsiveMediaService.toDeliveryResponse(mediaSet,
                                lang);
                        expandedFields.put(fieldKey, expanded);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to expand MEDIA field '{}': {}", fieldKey, e.getMessage());
            }
        }

        return expandedFields;
    }

    /**
     * Expands MEDIA fields for multiple entries at once.
     * Optimized for batch processing in delivery services.
     *
     * @param entriesCustomFields Map of entry ID to custom fields
     * @param componentTypeId     The component type ID
     * @param lang                The language for i18n content
     * @return Map of entry ID to expanded custom fields
     */
    public Map<Long, Map<String, Object>> expandMediaFieldsBatch(
            Map<Long, Map<String, Object>> entriesCustomFields,
            Long componentTypeId,
            Language lang) {

        if (entriesCustomFields == null || entriesCustomFields.isEmpty() || componentTypeId == null) {
            return entriesCustomFields != null ? entriesCustomFields : new HashMap<>();
        }

        // Get field definitions once for all entries
        List<EntryFieldDefinition> fieldDefinitions = entryFieldDefinitionRepository
                .findByComponentTypeId(componentTypeId);

        Set<String> mediaFieldKeys = fieldDefinitions.stream()
                .filter(f -> f.getFieldType() == EntryFieldType.MEDIA)
                .map(EntryFieldDefinition::getFieldKey)
                .collect(Collectors.toSet());

        if (mediaFieldKeys.isEmpty()) {
            return entriesCustomFields;
        }

        // Collect all media IDs from all entries
        Set<Long> allMediaIds = entriesCustomFields.values().stream()
                .flatMap(fields -> mediaFieldKeys.stream()
                        .map(fields::get)
                        .filter(value -> value != null)
                        .map(this::convertToLong)
                        .filter(id -> id != null))
                .collect(Collectors.toSet());

        if (allMediaIds.isEmpty()) {
            return entriesCustomFields;
        }

        // Batch fetch all ResponsiveMediaSets
        Map<Long, ResponsiveMediaSet> mediaSetMap = responsiveMediaSetRepository.findByIdIn(allMediaIds)
                .stream()
                .collect(Collectors.toMap(ResponsiveMediaSet::getId, m -> m));

        // Pre-convert all needed media sets to delivery responses
        Map<Long, ResponsiveMediaDeliveryResponse> deliveryResponseMap = mediaSetMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> responsiveMediaService.toDeliveryResponse(e.getValue(), lang)));

        // Expand fields for each entry
        Map<Long, Map<String, Object>> result = new HashMap<>();

        for (Map.Entry<Long, Map<String, Object>> entry : entriesCustomFields.entrySet()) {
            Map<String, Object> expandedFields = new HashMap<>(entry.getValue());

            for (String fieldKey : mediaFieldKeys) {
                Object value = entry.getValue().get(fieldKey);
                if (value == null) {
                    continue;
                }

                try {
                    Long responsiveMediaId = convertToLong(value);
                    if (responsiveMediaId != null && deliveryResponseMap.containsKey(responsiveMediaId)) {
                        expandedFields.put(fieldKey, deliveryResponseMap.get(responsiveMediaId));
                    }
                } catch (Exception e) {
                    log.warn("Failed to expand MEDIA field '{}' for entry {}: {}", fieldKey, entry.getKey(),
                            e.getMessage());
                }
            }

            result.put(entry.getKey(), expandedFields);
        }

        return result;
    }

    /**
     * Converts various numeric types to Long.
     */
    private Long convertToLong(Object value) {
        if (value instanceof Long l) {
            return l;
        } else if (value instanceof Integer i) {
            return i.longValue();
        } else if (value instanceof Number n) {
            return n.longValue();
        } else if (value instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Parses custom data JSON string to Map.
     */
    public Map<String, Object> parseCustomData(String customDataJson) {
        if (customDataJson == null || customDataJson.isBlank()) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(customDataJson, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            log.warn("Failed to parse custom_data JSON: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
