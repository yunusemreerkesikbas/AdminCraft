package com.backend.presentation.dto.response;

/**
 * DTO representing a single sort option.
 * Used in paginated responses to inform frontend about available sorting
 * options.
 */
public record SortOptionDto(
    String code, // "createdAt,desc"
    String labelKey, // "admin.sort.newest" (i18n key, frontend translates)
    boolean isDefault) {
  public static SortOptionDto of(String code, String labelKey) {
    return new SortOptionDto(code, labelKey, false);
  }

  public static SortOptionDto defaultOption(String code, String labelKey) {
    return new SortOptionDto(code, labelKey, true);
  }
}
