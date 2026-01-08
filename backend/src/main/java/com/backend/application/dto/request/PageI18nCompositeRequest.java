package com.backend.application.dto.request;

import com.backend.domain.enums.PageStatus;

import jakarta.validation.constraints.Size;

/**
 * I18n request DTO for composite operations.
 * Unlike PageI18nRequest, this doesn't include the language field
 * as it's used as a value in Map<Language, PageI18nCompositeRequest>.
 */
public record PageI18nCompositeRequest(
    @Size(max = 200, message = "validation.name.size") String name,
    @Size(max = 255, message = "validation.canonical.url.size") String canonicalUrl,
    @Size(max = 200, message = "validation.title.size") String title,
    String description,
    PageStatus status) {

  public PageI18nCompositeRequest {
    if (name != null) {
      name = name.trim();
    }
    if (canonicalUrl != null && !canonicalUrl.trim().isEmpty()) {
      canonicalUrl = canonicalUrl.trim().toLowerCase();
    }
    if (title != null) {
      title = title.trim();
    }
  }
}
