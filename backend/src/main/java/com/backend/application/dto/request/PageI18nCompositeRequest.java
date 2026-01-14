package com.backend.application.dto.request;

import com.backend.domain.enums.PageStatus;

import jakarta.validation.constraints.Size;

import static com.backend.shared.constants.ValidationConstants.PAGE_CANONICAL_URL_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.PAGE_DESCRIPTION_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.PAGE_NAME_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.PAGE_TITLE_MAX_LENGTH;

/**
 * I18n request DTO for composite operations.
 * Unlike PageI18nRequest, this doesn't include the language field
 * as it's used as a value in Map<Language, PageI18nCompositeRequest>.
 */
public record PageI18nCompositeRequest(
  @Size(max = PAGE_NAME_MAX_LENGTH, message = "validation.page.name.size") String name,
  @Size(max = PAGE_CANONICAL_URL_MAX_LENGTH, message = "validation.page.canonicalUrl.size") String canonicalUrl,
  @Size(max = PAGE_TITLE_MAX_LENGTH, message = "validation.page.title.size") String title,
  @Size(max = PAGE_DESCRIPTION_MAX_LENGTH, message = "validation.page.description.size") String description,
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
