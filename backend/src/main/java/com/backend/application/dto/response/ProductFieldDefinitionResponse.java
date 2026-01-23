package com.backend.application.dto.response;

import java.util.Map;

import com.backend.domain.entity.ProductFieldDefinition;
import com.backend.domain.enums.ProductFieldType;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response DTO for product field definition.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductFieldDefinitionResponse(
    Long id,
    String code,
    String name,
    ProductFieldType fieldType,
    Boolean isRequired,
    Boolean isVisibleInList,
    Integer sortOrder,
    String defaultValue,
    Map<String, Object> validationConfig) {
  /**
   * Factory method to create response from entity.
   */
  public static ProductFieldDefinitionResponse from(ProductFieldDefinition entity) {
    return from(entity, null);
  }

  /**
   * Factory method to create response from entity with parsed validation config.
   */
  public static ProductFieldDefinitionResponse from(ProductFieldDefinition entity,
      Map<String, Object> parsedValidationConfig) {
    return new ProductFieldDefinitionResponse(
        entity.getId(),
        entity.getCode(),
        entity.getName(),
        entity.getFieldType(),
        entity.getIsRequired(),
        entity.getIsVisibleInList(),
        entity.getSortOrder(),
        entity.getDefaultValue(),
        parsedValidationConfig);
  }
}
