package com.backend.application.service;

import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.ComponentEntryI18nRepository;
import com.backend.domain.repository.ComponentEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        entryI18n.setTitle((String) data.get("title"));
        entryI18n.setDescription((String) data.get("description"));
        entryI18n.setImageUrl((String) data.get("imageUrl"));
        entryI18n.setButtonText((String) data.get("buttonText"));
        entryI18n.setButtonUrl((String) data.get("buttonUrl"));

        ComponentEntryI18n saved = entryI18nRepository.save(entryI18n);

        updateDynamicFields(saved.getId(), data);

        log.info("Upserted entry i18n {} for entry {} in language {}", saved.getId(), entryId, language);
        return saved;
    }

    private void updateDynamicFields(Long entryI18nId, Map<String, Object> data) {
        data.forEach((key, value) -> {
            if (!isBaseField(key) && value != null) {
                String sql = String.format("UPDATE component_entry_i18n SET %s = ? WHERE id = ?", key);
                jdbcTemplate.update(sql, value, entryI18nId);
            }
        });
    }

    private boolean isBaseField(String key) {
        return List.of("title", "description", "imageUrl", "buttonText", "buttonUrl", "status").contains(key);
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



