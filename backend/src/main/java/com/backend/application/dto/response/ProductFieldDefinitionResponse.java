package com.backend.application.dto.response;

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
    ProductFieldType fieldType) {
  /**
   * Factory method to create response from entity.
   */
  public static ProductFieldDefinitionResponse from(ProductFieldDefinition entity,
      Object ignored) {
    return new ProductFieldDefinitionResponse(
        entity.getId(),
        entity.getCode(),
        entity.getName(),
        entity.getFieldType());
  }
}
