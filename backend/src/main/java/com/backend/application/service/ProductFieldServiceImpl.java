package com.backend.application.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.request.CreateProductFieldRequest;
import com.backend.application.dto.request.UpdateProductFieldRequest;
import com.backend.application.dto.response.ProductFieldDefinitionResponse;
import com.backend.domain.entity.Product;
import com.backend.domain.entity.ProductFieldDefinition;
import com.backend.domain.entity.ProductFieldValue;
import com.backend.domain.exception.BusinessRuleViolationException;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.domain.repository.ProductFieldDefinitionRepository;
import com.backend.domain.repository.ProductFieldValueRepository;
import com.backend.infrastructure.tenant.TenantContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductFieldServiceImpl implements ProductFieldService {

  private final ProductFieldDefinitionRepository definitionRepository;
  private final ProductFieldValueRepository valueRepository;
  private final ObjectMapper objectMapper;

  @Override
  @Transactional(readOnly = true)
  public List<ProductFieldDefinitionResponse> findAllDefinitions() {
    TenantContext.validateActive();
    return definitionRepository.findAllByOrderBySortOrderAsc()
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProductFieldDefinitionResponse> findVisibleDefinitions() {
    TenantContext.validateActive();
    return definitionRepository.findByIsVisibleInListTrueOrderBySortOrderAsc()
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public ProductFieldDefinitionResponse findDefinitionById(Long id) {
    TenantContext.validateActive();
    ProductFieldDefinition definition = definitionRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("ProductFieldDefinition", id));
    return toResponse(definition);
  }

  @Override
  @Transactional
  public ProductFieldDefinitionResponse createDefinition(CreateProductFieldRequest request) {
    TenantContext.validateActive();

    // Check for duplicate code
    if (definitionRepository.existsByCode(request.code())) {
      throw new BusinessRuleViolationException("Field with code '" + request.code() + "' already exists");
    }

    // Check for duplicate uid
    if (definitionRepository.existsByUid(request.uid())) {
      throw new BusinessRuleViolationException("Field with uid '" + request.uid() + "' already exists");
    }

    ProductFieldDefinition definition = ProductFieldDefinition.builder()
        .uid(request.uid())
        .code(request.code())
        .name(request.name())
        .fieldType(request.fieldType())
        .isRequired(request.isRequired())
        .isVisibleInList(request.isVisibleInList())
        .sortOrder(request.sortOrder())
        .defaultValue(request.defaultValue())
        .validationConfig(serializeValidationConfig(request.validationConfig()))
        .build();

    definition = definitionRepository.save(definition);
    log.info("Created product field definition: {} ({})", definition.getCode(), definition.getId());

    return toResponse(definition);
  }

  @Override
  @Transactional
  public ProductFieldDefinitionResponse updateDefinition(Long id, UpdateProductFieldRequest request) {
    TenantContext.validateActive();
    ProductFieldDefinition definition = definitionRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("ProductFieldDefinition", id));

    if (request.name() != null) {
      definition.setName(request.name());
    }
    if (request.fieldType() != null) {
      definition.setFieldType(request.fieldType());
    }
    if (request.isRequired() != null) {
      definition.setIsRequired(request.isRequired());
    }
    if (request.isVisibleInList() != null) {
      definition.setIsVisibleInList(request.isVisibleInList());
    }
    if (request.sortOrder() != null) {
      definition.setSortOrder(request.sortOrder());
    }
    if (request.defaultValue() != null) {
      definition.setDefaultValue(request.defaultValue());
    }
    if (request.validationConfig() != null) {
      definition.setValidationConfig(serializeValidationConfig(request.validationConfig()));
    }

    definition = definitionRepository.save(definition);
    log.info("Updated product field definition: {} ({})", definition.getCode(), definition.getId());

    return toResponse(definition);
  }

  @Override
  @Transactional
  public void deleteDefinition(Long id) {
    TenantContext.validateActive();
    if (!definitionRepository.existsById(id)) {
      throw new EntityNotFoundException("ProductFieldDefinition", id);
    }

    // Delete all associated values first (fetch then delete for audit compliance)
    List<ProductFieldValue> values = valueRepository.findByFieldDefinitionId(id);
    if (!values.isEmpty()) {
      valueRepository.deleteAll(values);
      log.debug("Deleted {} field values for definition {}", values.size(), id);
    }

    // Delete the definition
    definitionRepository.deleteById(id);
    log.info("Deleted product field definition: {}", id);
  }

  @Override
  @Transactional
  public void saveFieldValues(Product product, Map<String, Object> fieldValues) {
    TenantContext.validateActive();
    if (fieldValues == null || fieldValues.isEmpty()) {
      return;
    }

    // Get only the definitions we need by code
    List<String> codes = fieldValues.keySet().stream().toList();
    Map<String, ProductFieldDefinition> definitionsByCode = definitionRepository.findByCodeIn(codes)
        .stream()
        .collect(Collectors.toMap(ProductFieldDefinition::getCode, d -> d));

    // Get existing values for this product with definitions eagerly loaded
    Map<Long, ProductFieldValue> existingValues = valueRepository.findByProductIdWithDefinition(product.getId())
        .stream()
        .collect(Collectors.toMap(v -> v.getFieldDefinition().getId(), v -> v));

    for (Map.Entry<String, Object> entry : fieldValues.entrySet()) {
      String code = entry.getKey();
      Object value = entry.getValue();

      ProductFieldDefinition definition = definitionsByCode.get(code);
      if (definition == null) {
        log.warn("Unknown field code '{}', skipping", code);
        continue;
      }

      ProductFieldValue fieldValue = existingValues.get(definition.getId());
      if (fieldValue == null) {
        fieldValue = ProductFieldValue.builder()
            .product(product)
            .fieldDefinition(definition)
            .build();
      }

      fieldValue.setValue(value);
      valueRepository.save(fieldValue);
    }

    log.debug("Saved {} field values for product {}", fieldValues.size(), product.getId());
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> getFieldValues(Long productId) {
    TenantContext.validateActive();
    List<ProductFieldValue> values = valueRepository.findByProductIdWithDefinition(productId);

    Map<String, Object> result = new LinkedHashMap<>();
    for (ProductFieldValue value : values) {
      String code = value.getFieldDefinition().getCode();
      Object val = value.getValue();
      if (val != null) {
        result.put(code, val);
      }
    }

    return result;
  }

  @Override
  @Transactional
  public void deleteFieldValues(Long productId) {
    TenantContext.validateActive();
    // Fetch entities first to ensure entity lifecycle events are triggered
    List<ProductFieldValue> values = valueRepository.findByProductId(productId);
    if (!values.isEmpty()) {
      valueRepository.deleteAll(values);
    }
    log.debug("Deleted {} field values for product {}", values.size(), productId);
  }

  // ========================
  // Private helper methods
  // ========================

  private ProductFieldDefinitionResponse toResponse(ProductFieldDefinition entity) {
    Map<String, Object> validationConfig = parseValidationConfig(entity.getValidationConfig());
    return ProductFieldDefinitionResponse.from(entity, validationConfig);
  }

  private String serializeValidationConfig(Map<String, Object> config) {
    if (config == null || config.isEmpty()) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(config);
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize validation config", e);
      return null;
    }
  }

  private Map<String, Object> parseValidationConfig(String json) {
    if (json == null || json.isBlank()) {
      return null;
    }
    try {
      return objectMapper.readValue(json, new TypeReference<>() {
      });
    } catch (JsonProcessingException e) {
      log.error("Failed to parse validation config", e);
      return null;
    }
  }
}
