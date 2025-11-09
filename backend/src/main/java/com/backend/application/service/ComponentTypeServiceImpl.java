package com.backend.application.service;

import com.backend.application.command.ComponentTypeCommands.*;
import com.backend.application.query.ComponentTypeQueries.*;
import com.backend.domain.entity.ComponentType;
import com.backend.domain.repository.ComponentTypeRepository;
import com.backend.infrastructure.util.UuidUidGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComponentTypeServiceImpl implements ComponentTypeService {

    private final ComponentTypeRepository componentTypeRepository;
    private final ComponentSchemaValidator schemaValidator;

    @Override
    @Transactional
    public ComponentType createComponentType(CreateComponentTypeCommand command) {
        log.debug("Creating component type with code: {}", command.code());

        JsonNode sanitizedSchema = null;
        if (command.extendedFieldsSchema() != null && !command.extendedFieldsSchema().isNull()) {
            try {
                sanitizedSchema = schemaValidator.validateSchema(command.extendedFieldsSchema());
                log.debug("Schema validated successfully for component type: {}", command.code());
            } catch (IllegalArgumentException e) {
                log.error("Schema validation failed for component type {}: {}", command.code(), e.getMessage());
                throw new IllegalArgumentException("Invalid extended fields schema: " + e.getMessage(), e);
            }
        }

        ComponentType componentType = new ComponentType();
        componentType.setCode(command.code());
        componentType.setName(command.name());
        componentType.setCategory(command.category());
        componentType.setIcon(command.icon());
        componentType.setExtendedFieldsSchema(sanitizedSchema);
        componentType.setIsSystem(false);
        componentType.setCreatedBy(command.userId());
        componentType.setUpdatedBy(command.userId());

        componentType = componentTypeRepository.save(componentType);
        log.info("Component type created successfully with id: {} and code: {}", componentType.getId(),
                componentType.getCode());
        return componentType;
    }

    @Override
    @Transactional(readOnly = true)
    public ComponentType getComponentTypeById(GetComponentTypeByIdQuery query) {
        return componentTypeRepository.findById(query.id())
                .orElseThrow(() -> new IllegalArgumentException("ComponentType not found with id: " + query.id()));
    }

    @Override
    @Transactional(readOnly = true)
    public ComponentType getComponentTypeByCode(GetComponentTypeByCodeQuery query) {
        return componentTypeRepository.findByCode(query.code())
                .orElseThrow(() -> new IllegalArgumentException("ComponentType not found with code: " + query.code()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComponentType> getAllComponentTypes(GetAllComponentTypesQuery query) {
        return componentTypeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComponentType> getComponentTypesByCategory(GetComponentTypesByCategoryQuery query) {
        return componentTypeRepository.findByCategory(query.category());
    }

    @Override
    @Transactional
    public ComponentType updateComponentType(UpdateComponentTypeCommand command) {
        log.debug("Updating component type with id: {}", command.id());

        ComponentType componentType = componentTypeRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("ComponentType not found with id: " + command.id()));

        if (componentType.getIsSystem()) {
            throw new IllegalStateException("Cannot update system component type");
        }

        JsonNode sanitizedSchema = null;
        if (command.extendedFieldsSchema() != null && !command.extendedFieldsSchema().isNull()) {
            try {
                sanitizedSchema = schemaValidator.validateSchema(command.extendedFieldsSchema());
                log.debug("Schema validated successfully for component type update: {}", command.id());
            } catch (IllegalArgumentException e) {
                log.error("Schema validation failed for component type {}: {}", command.id(), e.getMessage());
                throw new IllegalArgumentException("Invalid extended fields schema: " + e.getMessage(), e);
            }
        }

        componentType.setCode(command.code());
        componentType.setName(command.name());
        componentType.setCategory(command.category());
        componentType.setIcon(command.icon());
        componentType.setExtendedFieldsSchema(sanitizedSchema);
        componentType.setUpdatedBy(command.userId());

        componentType = componentTypeRepository.save(componentType);
        log.info("Component type updated successfully with id: {}", command.id());
        return componentType;
    }

    @Override
    @Transactional
    public void deleteComponentType(DeleteComponentTypeCommand command) {
        ComponentType componentType = componentTypeRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("ComponentType not found with id: " + command.id()));

        if (componentType.getIsSystem()) {
            throw new IllegalStateException("Cannot delete system component type");
        }

        componentTypeRepository.delete(componentType);
    }

    @Override
    public JsonNode validateSchema(JsonNode schema) {
        log.debug("Validating component type schema");
        try {
            JsonNode sanitizedSchema = schemaValidator.validateSchema(schema);
            log.debug("Schema validation successful");
            return sanitizedSchema;
        } catch (IllegalArgumentException e) {
            log.error("Schema validation failed: {}", e.getMessage());
            throw new IllegalArgumentException("Schema validation failed: " + e.getMessage(), e);
        }
    }

    private String generateUniqueUid() {
        String uid;
        do {
            uid = UuidUidGenerator.generateUid();
        } while (componentTypeRepository.existsByUid(uid));
        return uid;
    }
}
