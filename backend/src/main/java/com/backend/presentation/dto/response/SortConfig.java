package com.backend.presentation.dto.response;

import java.util.List;

/**
 * DTO containing current sort info and available sort options.
 * Embedded in paginated responses for dynamic sort UI.
 */
public record SortConfig(
    CurrentSort currentSort,
    List<SortOptionDto> availableSorts) {
  /**
   * Represents the currently applied sort.
   */
  public record CurrentSort(
      String field,
      String direction,
      String code) {
    public static CurrentSort from(String sortCode) {
      if (sortCode == null || sortCode.isBlank()) {
        return null;
      }
      String[] parts = sortCode.split(",");
      if (parts.length != 2) {
        return null;
      }
      return new CurrentSort(parts[0].trim(), parts[1].trim().toLowerCase(), sortCode);
    }
  }

  public static SortConfig of(String currentSortCode, List<SortOptionDto> availableSorts) {
    return new SortConfig(CurrentSort.from(currentSortCode), availableSorts);
  }
}
