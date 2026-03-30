package com.backend.application.service;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import org.owasp.encoder.Encode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.ComponentEntryI18nRepository;
import com.backend.domain.repository.ComponentEntryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComponentEntryI18nServiceImpl implements ComponentEntryI18nService {

    private final ComponentEntryI18nRepository entryI18nRepository;
    private final ComponentEntryRepository entryRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ComponentEntryI18n upsertEntryI18n(Long entryId, Language language, Map<String, Object> data) {
        entryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Entry not found: " + entryId));

        ComponentEntryI18n entryI18n = entryI18nRepository
                .findByEntryIdAndLanguage(entryId, language)
                .orElse(null);

        if (entryI18n == null) {
            entryI18n = new ComponentEntryI18n();
            entryI18n.setEntryId(entryId);
            entryI18n.setLanguage(language);
        }

        entryI18n.setTitle(sanitizeString((String) data.get("title")));
        entryI18n.setDescription(sanitizeHtml((String) data.get("description")));
        entryI18n.setCustomData(mergeCustomData(entryI18n.getCustomData(), data));

        ComponentEntryI18n saved = entryI18nRepository.save(entryI18n);

        log.info("Upserted entry i18n {} for entry {} in language {}", saved.getId(), entryId, language);
        return saved;
    }

    private String mergeCustomData(String existingCustomData, Map<String, Object> data) {
        Map<String, Object> customFields = new LinkedHashMap<>(parseCustomData(existingCustomData));

        data.forEach((key, value) -> {
            if (!isBaseField(key) && value != null) {
                validateFieldKey(key);
                customFields.put(key, sanitizeCustomFieldValue(key, value));
            }
        });

        if (customFields.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(customFields);
        } catch (Exception ex) {
            log.error("Failed to serialize component entry i18n custom data", ex);
            throw new IllegalStateException("error.runtime", ex);
        }
    }

    protected void validateFieldKey(String fieldKey) {
        if (!fieldKey.matches("^[a-z][a-zA-Z0-9]{0,49}$")) {
            throw new IllegalArgumentException("Invalid field key: " + fieldKey);
        }
    }

    private boolean isBaseField(String key) {
        return List.of("title", "description", "status").contains(key);
    }

    protected String sanitizeString(String value) {
        if (value == null)
            return null;
        return Encode.forHtml(value);
    }

    protected String sanitizeHtml(String value) {
        if (value == null)
            return null;
        return Encode.forHtmlContent(value);
    }

    protected String sanitizeUrl(String value) {
        if (value == null)
            return null;
        return Encode.forUriComponent(value);
    }

    protected Object sanitizeCustomFieldValue(String key, Object value) {
        if (value instanceof Map<?, ?> mapValue) {
            Map<String, Object> sanitized = new LinkedHashMap<>();
            mapValue.forEach((nestedKey, nestedValue) -> {
                if (nestedKey != null) {
                    sanitized.put(String.valueOf(nestedKey), sanitizeCustomFieldValue(String.valueOf(nestedKey), nestedValue));
                }
            });
            return sanitized;
        }
        if (value instanceof List<?> listValue) {
            return listValue.stream()
                    .map(item -> sanitizeCustomFieldValue(key, item))
                    .toList();
        }
        if (value instanceof String) {
            if ("imageUrl".equals(key) || "buttonUrl".equals(key)) {
                return sanitizeUrl((String) value);
            }
            return sanitizeString((String) value);
        }
        return value;
    }

    private Map<String, Object> parseCustomData(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            log.error("Failed to parse component entry i18n custom data", ex);
            throw new IllegalStateException("error.runtime", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ComponentEntryI18n getEntryI18n(Long entryId, Language language) {
        return entryI18nRepository.findByEntryIdAndLanguage(entryId, language)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Entry i18n not found for entry " + entryId + " and language " + language));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComponentEntryI18n> getEntryI18nByEntryId(Long entryId) {
        return entryI18nRepository.findByEntryId(entryId);
    }

    @Override
    @Transactional
    public ComponentEntryI18n publishEntryI18n(Long entryId, Language language) {
        ComponentEntryI18n entryI18n = entryI18nRepository
                .findByEntryIdAndLanguage(entryId, language)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Entry i18n not found for entry " + entryId + " and language " + language));

        entryI18n.publish();
        ComponentEntryI18n saved = entryI18nRepository.save(entryI18n);
        log.info("Published entry i18n {} for entry {} in language {}", saved.getId(), entryId, language);
        return saved;
    }

    @Override
    @Transactional
    public void deleteEntryI18n(Long entryId, Language language) {
        ComponentEntryI18n entryI18n = entryI18nRepository
                .findByEntryIdAndLanguage(entryId, language)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Entry i18n not found for entry " + entryId + " and language " + language));

        entryI18nRepository.delete(entryI18n);
        log.info("Deleted entry i18n for entry {} and language {}", entryId, language);
    }
}
