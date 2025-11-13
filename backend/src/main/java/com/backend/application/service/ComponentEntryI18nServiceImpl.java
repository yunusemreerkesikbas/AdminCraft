package com.backend.application.service;

import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.ComponentEntryI18nRepository;
import com.backend.domain.repository.ComponentEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComponentEntryI18nServiceImpl implements ComponentEntryI18nService {

    private final ComponentEntryI18nRepository entryI18nRepository;
    private final ComponentEntryRepository entryRepository;
    private final JdbcTemplate jdbcTemplate;

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
        entryI18n.setImageUrl(sanitizeUrl((String) data.get("imageUrl")));
        entryI18n.setButtonText(sanitizeString((String) data.get("buttonText")));
        entryI18n.setButtonUrl(sanitizeUrl((String) data.get("buttonUrl")));

        ComponentEntryI18n saved = entryI18nRepository.save(entryI18n);

        updateDynamicFields(saved.getId(), data);

        log.info("Upserted entry i18n {} for entry {} in language {}", saved.getId(), entryId, language);
        return saved;
    }

    private void updateDynamicFields(Long entryI18nId, Map<String, Object> data) {
        data.forEach((key, value) -> {
            if (!isBaseField(key) && value != null) {
                validateFieldKey(key);
                String escapedColumn = escapeIdentifier(key);
                String sql = String.format("UPDATE component_entry_i18n SET %s = ? WHERE id = ?", escapedColumn);
                Object sanitizedValue = sanitizeValue(value);
                jdbcTemplate.update(sql, sanitizedValue, entryI18nId);
            }
        });
    }

    protected void validateFieldKey(String fieldKey) {
        if (!fieldKey.matches("^[a-z][a-zA-Z0-9]{0,49}$")) {
            throw new IllegalArgumentException("Invalid field key: " + fieldKey);
        }
    }

    protected String escapeIdentifier(String identifier) {
        String cleaned = identifier.replace("`", "``");
        return "`" + cleaned + "`";
    }

    private boolean isBaseField(String key) {
        return List.of("title", "description", "imageUrl", "buttonText", "buttonUrl", "status").contains(key);
    }

    protected String sanitizeString(String value) {
        if (value == null) return null;
        return Encode.forHtml(value);
    }

    protected String sanitizeHtml(String value) {
        if (value == null) return null;
        return Encode.forHtmlContent(value);
    }

    protected String sanitizeUrl(String value) {
        if (value == null) return null;
        return Encode.forUriComponent(value);
    }

    protected Object sanitizeValue(Object value) {
        if (value instanceof String) {
            return sanitizeString((String) value);
        }
        return value;
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
        return entryI18nRepository.save(entryI18n);
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



