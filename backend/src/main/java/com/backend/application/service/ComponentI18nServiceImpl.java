package com.backend.application.service;

import com.backend.application.command.ComponentI18nCommands.*;
import com.backend.application.query.ComponentI18nQueries.*;
import com.backend.application.query.ComponentTypeQueries.GetComponentTypeByIdQuery;
import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.repository.ComponentI18nRepository;
import com.backend.domain.repository.ComponentRepository;
import com.backend.infrastructure.util.UuidUidGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComponentI18nServiceImpl implements ComponentI18nService {

    private final ComponentI18nRepository componentI18nRepository;
    private final ComponentRepository componentRepository;
    private final ComponentTypeService componentTypeService;

    @Override
    @Transactional
    public ComponentI18n upsertComponentI18n(UpsertComponentI18nCommand command) {
        Component component = componentRepository.findById(command.componentId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Component not found with id: " + command.componentId()));

        JsonNode validatedExtendedLocalizedData = null;
        if (command.extendedLocalizedData() != null) {
            var componentType = componentTypeService.getComponentTypeById(
                    new GetComponentTypeByIdQuery(component.getComponentTypeId()));
            validatedExtendedLocalizedData = validateExtendedLocalizedData(
                    componentType.getExtendedFieldsSchema(),
                    command.extendedLocalizedData());
        }

        ComponentI18n componentI18n = componentI18nRepository
                .findByComponentIdAndLanguage(command.componentId(), command.language())
                .orElse(null);

        if (componentI18n == null) {
            componentI18n = new ComponentI18n();
            componentI18n.setComponentId(command.componentId());
            componentI18n.setLanguage(command.language());
        }

        componentI18n.setBaseLocalizedData(command.baseLocalizedData());
        componentI18n.setExtendedLocalizedData(validatedExtendedLocalizedData);
        if (command.status() != null) {
            componentI18n.setStatus(command.status());
        }

        return componentI18nRepository.save(componentI18n);
    }

    @Override
    @Transactional(readOnly = true)
    public ComponentI18n getComponentI18n(GetComponentI18nQuery query) {
        return componentI18nRepository
                .findByComponentIdAndLanguage(query.componentId(), query.language())
                .orElseThrow(() -> new IllegalArgumentException(
                        "ComponentI18n not found for componentId: " + query.componentId() + " and language: "
                                + query.language()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComponentI18n> getComponentI18nByComponentId(GetComponentI18nByComponentIdQuery query) {
        return componentI18nRepository.findByComponentId(query.componentId());
    }

    @Override
    @Transactional
    public ComponentI18n publishComponentI18n(PublishComponentI18nCommand command) {
        ComponentI18n componentI18n = componentI18nRepository
                .findByComponentIdAndLanguage(command.componentId(), command.language())
                .orElseThrow(() -> new IllegalArgumentException(
                        "ComponentI18n not found for componentId: " + command.componentId() + " and language: "
                                + command.language()));

        componentI18n.publish();
        return componentI18nRepository.save(componentI18n);
    }

    @Override
    @Transactional
    public ComponentI18n unpublishComponentI18n(UnpublishComponentI18nCommand command) {
        ComponentI18n componentI18n = componentI18nRepository
                .findByComponentIdAndLanguage(command.componentId(), command.language())
                .orElseThrow(() -> new IllegalArgumentException(
                        "ComponentI18n not found for componentId: " + command.componentId() + " and language: "
                                + command.language()));

        componentI18n.unpublish();
        return componentI18nRepository.save(componentI18n);
    }

    @Override
    @Transactional
    public void deleteComponentI18n(DeleteComponentI18nCommand command) {
        ComponentI18n componentI18n = componentI18nRepository
                .findByComponentIdAndLanguage(command.componentId(), command.language())
                .orElseThrow(() -> new IllegalArgumentException(
                        "ComponentI18n not found for componentId: " + command.componentId() + " and language: "
                                + command.language()));
        componentI18nRepository.delete(componentI18n);
    }

    private JsonNode validateExtendedLocalizedData(JsonNode extendedFieldsSchema, JsonNode extendedLocalizedData) {
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
