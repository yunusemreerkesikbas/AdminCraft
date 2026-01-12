package com.backend.application.dto.request;

/**
 * Application layer DTO for category i18n data.
 * Used in composite category operations.
 * No validation annotations - validation happens in Presentation layer.
 */
public record CategoryI18nDto(
    String name,
    String description) {
  /**
   * Creates a DTO with only the required name field.
   */
  public CategoryI18nDto(String name) {
    this(name, null);
  }
}
