package com.backend.application.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.response.ComponentEntryCompositeResponse;
import com.backend.application.dto.response.ResponsiveMediaResponse;
import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.entity.Media;
import com.backend.domain.entity.ResponsiveMediaSet;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.ComponentEntryI18nRepository;
import com.backend.domain.repository.ComponentEntryRepository;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.ResponsiveMediaSetRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComponentEntryServiceImpl implements ComponentEntryService {

    private final ComponentEntryRepository entryRepository;
    private final ComponentEntryI18nRepository entryI18nRepository;
    private final ComponentRepository componentRepository;
    private final ResponsiveMediaSetRepository responsiveMediaSetRepository;
    private final MediaService mediaService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final ComponentMediaLinkSyncService componentMediaLinkSyncService;

    @Override
    @Transactional
    public ComponentEntry createEntry(ComponentEntry entry) {
        componentRepository.findById(entry.getComponentId())
                .orElseThrow(() -> new IllegalArgumentException("Component not found: " + entry.getComponentId()));

        ComponentEntry saved = entryRepository.save(entry);
        log.info("Created entry {} for component {}", saved.getId(), saved.getComponentId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public ComponentEntry getEntryById(Long id) {
        return entryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entry not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComponentEntry> getEntriesByComponentId(Long componentId) {
        return entryRepository.findByComponentIdOrderBySortOrder(componentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<ComponentEntry, List<ComponentEntryI18n>> getEntriesWithI18n(Long componentId) {
        List<ComponentEntry> entries = entryRepository.findByComponentIdOrderBySortOrder(componentId);
        List<Long> entryIds = entries.stream().map(ComponentEntry::getId).collect(Collectors.toList());

        Map<Long, List<ComponentEntryI18n>> i18nByEntry = new HashMap<>();
        for (Long entryId : entryIds) {
            i18nByEntry.put(entryId, entryI18nRepository.findByEntryId(entryId));
        }

        Map<ComponentEntry, List<ComponentEntryI18n>> result = new HashMap<>();
        for (ComponentEntry entry : entries) {
            result.put(entry, i18nByEntry.getOrDefault(entry.getId(), List.of()));
        }

        return result;
    }

    @Override
    @Transactional
    public ComponentEntry updateEntry(ComponentEntry entry) {
        ComponentEntry existing = entryRepository.findById(entry.getId())
                .orElseThrow(() -> new IllegalArgumentException("Entry not found: " + entry.getId()));

        existing.setSortOrder(entry.getSortOrder());
        existing.setIsVisible(entry.getIsVisible());
        existing.setStyleClasses(entry.getStyleClasses());
        existing.setStatus(entry.getStatus());
        existing.setUpdatedBy(entry.getUpdatedBy());

        return entryRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteEntry(Long id) {
        ComponentEntry entry = entryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entry not found: " + id));
        componentMediaLinkSyncService.removeEntryResponsiveLinks(id);
        entryRepository.delete(entry);
        log.info("Deleted entry {}", id);
    }

    @Override
    @Transactional
    public com.backend.application.dto.response.ComponentEntryCompositeResponse createComposite(
            com.backend.application.dto.request.CreateComponentEntryCompositeRequest request) {

        componentRepository.findById(request.componentId())
                .orElseThrow(() -> new IllegalArgumentException("Component not found: " + request.componentId()));

        ComponentEntry entry = new ComponentEntry();
        entry.setComponentId(request.componentId());
        entry.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        entry.setIsVisible(request.isVisible() != null ? request.isVisible() : true);
        entry.setStyleClasses(request.styleClasses());
        if (request.responsiveMediaId() != null) {
            ResponsiveMediaSet mediaSet = responsiveMediaSetRepository.findById(request.responsiveMediaId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "ResponsiveMediaSet not found: " + request.responsiveMediaId()));
            entry.setResponsiveMedia(mediaSet);
        }

        ComponentEntry savedEntry = entryRepository.save(entry);
        componentMediaLinkSyncService.syncEntryResponsiveLinks(savedEntry);

        List<ComponentEntryI18n> i18nList = request.translations().entrySet().stream()
                .map(e -> {
                    ComponentEntryI18n i18n = new ComponentEntryI18n();
                    i18n.setEntryId(savedEntry.getId());
                    i18n.setLanguage(e.getKey());
                    i18n.setTitle(e.getValue().title());
                    i18n.setDescription(e.getValue().description());

                    if (e.getValue().dynamicFields() != null && !e.getValue().dynamicFields().isEmpty()) {
                        try {
                            i18n.setCustomData(objectMapper.writeValueAsString(e.getValue().dynamicFields()));
                        } catch (Exception ex) {
                            log.error("Failed to serialize custom fields for language {}", e.getKey(), ex);
                            throw new RuntimeException("Failed to serialize custom fields", ex);
                        }
                    }

                    return i18n;
                })
                .collect(Collectors.toList());

        List<ComponentEntryI18n> savedI18n = entryI18nRepository.saveAll(i18nList);

        log.info("Created entry composite: entryId={}, translations={}", savedEntry.getId(), savedI18n.size());

        return toCompositeResponse(savedEntry, savedI18n);
    }

    @Override
    @Transactional
    public com.backend.application.dto.response.ComponentEntryCompositeResponse updateComposite(
            Long id, com.backend.application.dto.request.UpdateComponentEntryCompositeRequest request) {

        ComponentEntry entry = entryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entry not found: " + id));

        if (request.sortOrder() != null)
            entry.setSortOrder(request.sortOrder());
        if (request.isVisible() != null)
            entry.setIsVisible(request.isVisible());
        if (request.styleClasses() != null)
            entry.setStyleClasses(request.styleClasses());

        if (request.responsiveMediaId() != null) {
            ResponsiveMediaSet mediaSet = responsiveMediaSetRepository.findById(request.responsiveMediaId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "ResponsiveMediaSet not found: " + request.responsiveMediaId()));
            entry.setResponsiveMedia(mediaSet);
        }

        ComponentEntry savedEntry = entryRepository.save(entry);
        componentMediaLinkSyncService.syncEntryResponsiveLinks(savedEntry);

        List<ComponentEntryI18n> existingI18n = entryI18nRepository.findByEntryId(id);
        Map<com.backend.domain.enums.Language, ComponentEntryI18n> i18nMap = existingI18n.stream()
                .collect(Collectors.toMap(ComponentEntryI18n::getLanguage, i -> i));

        List<ComponentEntryI18n> updatedI18n = request.translations().entrySet().stream()
                .map(e -> {
                    ComponentEntryI18n i18n = i18nMap.getOrDefault(e.getKey(), new ComponentEntryI18n());
                    if (i18n.getId() == null) {
                        i18n.setEntryId(id);
                        i18n.setLanguage(e.getKey());

                    }

                    if (e.getValue().title() != null)
                        i18n.setTitle(e.getValue().title());
                    i18n.setDescription(e.getValue().description());

                    if (e.getValue().dynamicFields() != null && !e.getValue().dynamicFields().isEmpty()) {
                        try {
                            i18n.setCustomData(objectMapper.writeValueAsString(e.getValue().dynamicFields()));
                        } catch (Exception ex) {
                            log.error("Failed to serialize custom fields for entryId={}, language={}", id, e.getKey(),
                                    ex);
                            throw new RuntimeException("Failed to serialize custom fields", ex);
                        }
                    } else {
                        i18n.setCustomData(null);
                    }

                    return entryI18nRepository.save(i18n);
                })
                .collect(Collectors.toList());

        return toCompositeResponse(savedEntry, updatedI18n);
    }

    @Override
    @Transactional(readOnly = true)
    public com.backend.application.dto.response.ComponentEntryCompositeResponse getEntryWithTranslations(Long id) {
        ComponentEntry entry = entryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entry not found: " + id));

        List<ComponentEntryI18n> i18nList = entryI18nRepository.findByEntryId(id);

        return toCompositeResponse(entry, i18nList);
    }

    private ComponentEntryCompositeResponse toCompositeResponse(
            ComponentEntry entry,
            List<ComponentEntryI18n> i18nList) {
        return ComponentEntryCompositeResponse.from(entry, i18nList,
                buildCustomFieldsByLanguage(i18nList));
    }

    private Map<Language, Map<String, Object>> buildCustomFieldsByLanguage(List<ComponentEntryI18n> i18nList) {
        Map<Language, Map<String, Object>> customFieldsByLanguage = Optional.ofNullable(i18nList)
                .orElseGet(List::of)
                .stream()
                .collect(Collectors.toMap(
                        ComponentEntryI18n::getLanguage,
                        i18n -> parseCustomData(i18n.getCustomData()),
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new));

        List<String> mediaUids = customFieldsByLanguage.values().stream()
                .map(this::extractMediaUid)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (mediaUids.isEmpty()) {
            return customFieldsByLanguage;
        }

        Map<String, ResponsiveMediaResponse.MediaSummary> mediaByUid = mediaService.findByUids(mediaUids)
                .stream()
                .collect(Collectors.toMap(
                        Media::getUid,
                        ResponsiveMediaResponse.MediaSummary::from,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new));

        if (mediaByUid.isEmpty()) {
            return customFieldsByLanguage;
        }

        return customFieldsByLanguage.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> hydrateLegacyMedia(entry.getValue(), mediaByUid),
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new));
    }

    private Map<String, Object> hydrateLegacyMedia(
            Map<String, Object> customFields,
            Map<String, ResponsiveMediaResponse.MediaSummary> mediaByUid) {
        String mediaUid = extractMediaUid(customFields);
        if (mediaUid == null) {
            return customFields;
        }

        ResponsiveMediaResponse.MediaSummary media = mediaByUid.get(mediaUid);
        if (media == null || customFields.containsKey("media")) {
            return customFields;
        }

        Map<String, Object> hydratedFields = new HashMap<>(customFields);
        hydratedFields.put("media", media);
        return hydratedFields;
    }

    private String extractMediaUid(Map<String, Object> customFields) {
        Object mediaUid = customFields.get("mediaUid");
        if (mediaUid instanceof String value && !value.isBlank()) {
            return value;
        }
        return null;
    }

    private Map<String, Object> parseCustomData(String json) {
        if (json == null || json.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                    });
        } catch (Exception e) {
            log.error("Failed to parse custom data json (length: {})",
                    json != null ? json.length() : 0, e);
            return java.util.Collections.emptyMap();
        }
    }
}
