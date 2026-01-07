package com.backend.shared.common;

import java.util.Set;

import org.springframework.data.domain.Sort;

/**
 * Utility for parsing and validating sort parameters.
 * Provides security against SQL injection via whitelist validation.
 */
public final class SortParseUtil {

  private SortParseUtil() {
    // Utility class
  }

  /**
   * Parses sort string and validates against allowed fields.
   * 
   * @param sortString    Sort parameter (e.g., "createdAt,desc")
   * @param allowedFields Set of allowed field names
   * @param defaultSort   Default sort string if input is null/empty
   * @return Spring Sort object
   * @throws IllegalArgumentException if field is not in whitelist
   */
  public static Sort parse(String sortString, Set<String> allowedFields, String defaultSort) {
    String effectiveSort = (sortString == null || sortString.isBlank()) ? defaultSort : sortString;

    String[] parts = effectiveSort.split(",");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid sort format. Expected: field,direction");
    }

    String field = parts[0].trim();
    String direction = parts[1].trim().toLowerCase();

    // Whitelist validation (OWASP)
    if (!allowedFields.contains(field)) {
      throw new IllegalArgumentException("Invalid sort field: " + field);
    }

    Sort.Direction sortDirection = "desc".equals(direction)
        ? Sort.Direction.DESC
        : Sort.Direction.ASC;

    return Sort.by(sortDirection, field);
  }

  /**
   * Returns the effective sort code (input or default).
   */
  public static String getEffectiveSortCode(String sortString, String defaultSort) {
    return (sortString == null || sortString.isBlank()) ? defaultSort : sortString;
  }
}
