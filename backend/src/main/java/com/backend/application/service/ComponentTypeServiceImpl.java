package com.backend.application.service;

import com.backend.domain.entity.ComponentType;
import com.backend.domain.repository.ComponentTypeRepository;
import com.backend.infrastructure.util.UuidUidGenerator;
import com.backend.presentation.dto.request.ComponentTypeCreateRequest;
import com.backend.presentation.dto.response.ComponentTypeResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComponentTypeServiceImpl implements ComponentTypeService {

    private final ComponentTypeRepository componentTypeRepository;
    private final ComponentSchemaValidator schemaValidator;

    @Override
    @Transactional
    public ComponentTypeResponse createComponentType(ComponentTypeCreateRequest request, Long userId) {
        log.debug("Creating component type with code: {}", request.code());
        JsonNode sanitizedSchema = null;
        if (request.extendedFieldsSchema() != null && !request.extendedFieldsSchema().isNull()) {
            try {
                sanitizedSchema = schemaValidator.validateSchema(request.extendedFieldsSchema());
                log.debug("Schema validated successfully for component type: {}", request.code());
            } catch (IllegalArgumentException e) {
                log.error("Schema validation failed for component type {}: {}", request.code(), e.getMessage());
                throw new IllegalArgumentException("Invalid extended fields schema: " + e.getMessage(), e);
            }
        }

        ComponentType componentType = new ComponentType();
        componentType.setUuid(UuidUidGenerator.generateUuid());
        componentType.setUid(generateUniqueUid());
        componentType.setCode(request.code());
        componentType.setName(request.name());
        componentType.setCategory(request.category());
        componentType.setIcon(request.icon());
        componentType.setExtendedFieldsSchema(sanitizedSchema);
        componentType.setIsSystem(false);
        componentType.setCreatedAt(LocalDateTime.now());
        componentType.setUpdatedAt(LocalDateTime.now());
        componentType.setCreatedBy(userId);
        componentType.setUpdatedBy(userId);

        componentType = componentTypeRepository.save(componentType);
        log.info("Component type created successfully with id: {} and code: {}", componentType.getId(),
                componentType.getCode());
        return ComponentTypeResponse.from(componentType);
    }

    @Override
    @Transactional(readOnly = true)
    public ComponentTypeResponse getComponentTypeById(Long id) {
        ComponentType componentType = componentTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ComponentType not found with id: " + id));
        return ComponentTypeResponse.from(componentType);
    }

    @Override
    @Transactional(readOnly = true)
    public ComponentTypeResponse getComponentTypeByCode(String code) {
        ComponentType componentType = componentTypeRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("ComponentType not found with code: " + code));
        return ComponentTypeResponse.from(componentType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComponentTypeResponse> getAllComponentTypes() {
        return componentTypeRepository.findAll()
                .stream()
                .map(ComponentTypeResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComponentTypeResponse> getComponentTypesByCategory(String category) {
        return componentTypeRepository.findByCategory(category)
                .stream()
                .map(ComponentTypeResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ComponentTypeResponse updateComponentType(Long id, ComponentTypeCreateRequest request, Long userId) {
        log.debug("Updating component type with id: {}", id);

        ComponentType componentType = componentTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ComponentType not found with id: " + id));

        if (componentType.getIsSystem()) {
            throw new IllegalStateException("Cannot update system component type");
        }
        JsonNode sanitizedSchema = null;
        if (request.extendedFieldsSchema() != null && !request.extendedFieldsSchema().isNull()) {
            try {
                sanitizedSchema = schemaValidator.validateSchema(request.extendedFieldsSchema());
                log.debug("Schema validated successfully for component type update: {}", id);
            } catch (IllegalArgumentException e) {
                log.error("Schema validation failed for component type {}: {}", id, e.getMessage());
                throw new IllegalArgumentException("Invalid extended fields schema: " + e.getMessage(), e);
            }
        }

        componentType.setCode(request.code());
        componentType.setName(request.name());
        componentType.setCategory(request.category());
        componentType.setIcon(request.icon());
        componentType.setExtendedFieldsSchema(sanitizedSchema);
        componentType.setUpdatedAt(LocalDateTime.now());
        componentType.setUpdatedBy(userId);

        componentType = componentTypeRepository.save(componentType);
        log.info("Component type updated successfully with id: {}", id);
        return ComponentTypeResponse.from(componentType);
    }

    @Override
    @Transactional
    public void deleteComponentType(Long id) {
        ComponentType componentType = componentTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ComponentType not found with id: " + id));

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
