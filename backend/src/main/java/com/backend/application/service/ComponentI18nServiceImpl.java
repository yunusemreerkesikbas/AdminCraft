package com.backend.application.service;

import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.ComponentI18nRepository;
import com.backend.domain.repository.ComponentRepository;
import com.backend.infrastructure.util.UuidUidGenerator;
import com.backend.presentation.dto.request.ComponentI18nRequest;
import com.backend.presentation.dto.response.ComponentI18nResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComponentI18nServiceImpl implements ComponentI18nService {

    private final ComponentI18nRepository componentI18nRepository;
    private final ComponentRepository componentRepository;
    private final ComponentTypeService componentTypeService;

    @Override
    @Transactional
    public ComponentI18nResponse upsertComponentI18n(Long componentId, Language language,
            ComponentI18nRequest request) {
        Component component = componentRepository.findById(componentId)
                .orElseThrow(() -> new IllegalArgumentException("Component not found with id: " + componentId));
        JsonNode validatedExtendedLocalizedData = null;
        if (request.extendedLocalizedData() != null) {
            var componentType = componentTypeService.getComponentTypeById(component.getComponentTypeId());
            validatedExtendedLocalizedData = validateExtendedLocalizedData(
                    componentType.extendedFieldsSchema(),
                    request.extendedLocalizedData());
        }

        ComponentI18n componentI18n = componentI18nRepository
                .findByComponentIdAndLanguage(componentId, language)
                .orElse(null);

        if (componentI18n == null) {
            componentI18n = new ComponentI18n();
            componentI18n.setUuid(UuidUidGenerator.generateUuid());
            componentI18n.setUid(generateUniqueUid());
            componentI18n.setComponentId(componentId);
            componentI18n.setLanguage(language);
        }

        componentI18n.setBaseLocalizedData(request.baseLocalizedData());
        componentI18n.setExtendedLocalizedData(validatedExtendedLocalizedData);
        if (request.status() != null) {
            componentI18n.setStatus(request.status());
        }
        componentI18n.setUpdatedAt(LocalDateTime.now());

        componentI18n = componentI18nRepository.save(componentI18n);
        return ComponentI18nResponse.from(componentI18n);
    }

    @Override
    @Transactional(readOnly = true)
    public ComponentI18nResponse getComponentI18n(Long componentId, Language language) {
        ComponentI18n componentI18n = componentI18nRepository
                .findByComponentIdAndLanguage(componentId, language)
                .orElseThrow(() -> new IllegalArgumentException(
                        "ComponentI18n not found for componentId: " + componentId + " and language: " + language));
        return ComponentI18nResponse.from(componentI18n);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComponentI18nResponse> getComponentI18nByComponentId(Long componentId) {
        return componentI18nRepository.findByComponentId(componentId)
                .stream()
                .map(ComponentI18nResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ComponentI18nResponse publishComponentI18n(Long componentId, Language language) {
        ComponentI18n componentI18n = componentI18nRepository
                .findByComponentIdAndLanguage(componentId, language)
                .orElseThrow(() -> new IllegalArgumentException(
                        "ComponentI18n not found for componentId: " + componentId + " and language: " + language));

        componentI18n.publish();
        componentI18n = componentI18nRepository.save(componentI18n);
        return ComponentI18nResponse.from(componentI18n);
    }

    @Override
    @Transactional
    public ComponentI18nResponse unpublishComponentI18n(Long componentId, Language language) {
        ComponentI18n componentI18n = componentI18nRepository
                .findByComponentIdAndLanguage(componentId, language)
                .orElseThrow(() -> new IllegalArgumentException(
                        "ComponentI18n not found for componentId: " + componentId + " and language: " + language));

        componentI18n.unpublish();
        componentI18n = componentI18nRepository.save(componentI18n);
        return ComponentI18nResponse.from(componentI18n);
    }

    @Override
    @Transactional
    public void deleteComponentI18n(Long componentId, Language language) {
        ComponentI18n componentI18n = componentI18nRepository
                .findByComponentIdAndLanguage(componentId, language)
                .orElseThrow(() -> new IllegalArgumentException(
                        "ComponentI18n not found for componentId: " + componentId + " and language: " + language));
        componentI18nRepository.delete(componentI18n);
    }

    private JsonNode validateExtendedLocalizedData(
            JsonNode extendedFieldsSchema,
            JsonNode extendedLocalizedData) {
        if (extendedFieldsSchema == null || extendedLocalizedData == null) {
            return extendedLocalizedData;
        }
        JsonNode i18nFields = extendedFieldsSchema.get("i18n");
        if (i18nFields == null || !i18nFields.isArray()) {
            return extendedLocalizedData;
        }
        var fieldNames = extendedLocalizedData.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldKey = fieldNames.next();
            boolean foundInSchema = false;

            for (JsonNode fieldDef : i18nFields) {
                if (fieldDef.get("key").asText().equals(fieldKey)) {
                    foundInSchema = true;
                    // TODO Sprint 3: Add type-specific validation
                    break;
                }
            }

            if (!foundInSchema) {
                throw new IllegalArgumentException(
                        "Field '" + fieldKey + "' not defined in component type schema");
            }
        }

        return extendedLocalizedData;
    }

    private String generateUniqueUid() {
        String uid;
        do {
            uid = UuidUidGenerator.generateUid();
        } while (componentI18nRepository.existsByUid(uid));
        return uid;
    }
}
