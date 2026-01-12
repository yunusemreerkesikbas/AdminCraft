package com.backend.application.dto.request;

/**
 * Application layer DTO for product i18n data.
 * Used in composite product operations.
 * No validation annotations - validation happens in Presentation layer.
 */
public record ProductI18nDto(
    String name,
    String shortDescription,
    String description,
    String seoTitle,
    String seoDescription) {
  /**
   * Creates a DTO with only the required name field.
   */
  public ProductI18nDto(String name) {
    this(name, null, null, null, null);
  }
}
