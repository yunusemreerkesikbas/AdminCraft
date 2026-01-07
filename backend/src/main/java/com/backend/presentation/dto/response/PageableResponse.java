package com.backend.presentation.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Minimal paginated response with sort configuration.
 * Only includes essential fields: content, totalPages, totalElements,
 * sortConfig.
 * 
 * Removed fields (can be inferred):
 * - size: sent in request
 * - number: sent in request
 * - first: number === 0
 * - last: number === totalPages - 1
 * 
 * @param <T> Entity type
 */
public record PageableResponse<T>(
    List<T> content,
    int totalPages,
    long totalElements,
    SortConfig sortConfig) {

  /**
   * Creates PageableResponse from Spring Data Page.
   */
  public static <T> PageableResponse<T> from(Page<T> page, SortConfig sortConfig) {
    return new PageableResponse<>(
        page.getContent(),
        page.getTotalPages(),
        page.getTotalElements(),
        sortConfig);
  }

  /**
   * Creates PageableResponse from mapped content.
   */
  public static <T, R> PageableResponse<R> fromMapped(Page<T> page, List<R> mappedContent, SortConfig sortConfig) {
    return new PageableResponse<>(
        mappedContent,
        page.getTotalPages(),
        page.getTotalElements(),
        sortConfig);
  }
}
