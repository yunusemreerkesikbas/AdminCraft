package com.backend.application.dto.request;

import java.time.LocalDateTime;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.PageStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import static com.backend.shared.constants.ValidationConstants.PAGE_CANONICAL_URL_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.PAGE_DESCRIPTION_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.PAGE_NAME_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.PAGE_TITLE_MAX_LENGTH;

public record PageI18nRequest(
    @NotNull(message = "validation.language.required") Language language,
  @Size(max = PAGE_NAME_MAX_LENGTH, message = "validation.page.name.size") String name,
  @Size(max = PAGE_CANONICAL_URL_MAX_LENGTH, message = "validation.page.canonicalUrl.size") String canonicalUrl,
  @Size(max = PAGE_TITLE_MAX_LENGTH, message = "validation.page.title.size") String title,
  @Size(max = PAGE_DESCRIPTION_MAX_LENGTH, message = "validation.page.description.size") String description,
    PageStatus status,
    LocalDateTime scheduledAt) {

  public PageI18nRequest {
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
