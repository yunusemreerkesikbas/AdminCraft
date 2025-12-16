package com.backend.application.dto.request;

import java.time.LocalDateTime;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.PageStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PageI18nRequest(
    @NotNull(message = "validation.language.required") Language language,
    @Size(max = 255, message = "validation.url.path.size") String urlPath,
    @Size(max = 200, message = "validation.title.size") String title,
    @Size(max = 200, message = "validation.subtitle.size") String subtitle,
    @Size(max = 60, message = "validation.meta.title.size") String metaTitle,
    @Size(max = 160, message = "validation.meta.description.size") String metaDescription,
    String description,
    String descriptionHtml,
    PageStatus status,
    LocalDateTime scheduledAt) {

  public PageI18nRequest {
    if (urlPath != null && !urlPath.trim().isEmpty()) {
      urlPath = urlPath.trim().toLowerCase();
    }
    if (title != null) {
      title = title.trim();
    }
    if (subtitle != null) {
      subtitle = subtitle.trim();
    }
    if (metaTitle != null) {
      metaTitle = metaTitle.trim();
    }
    if (metaDescription != null) {
      metaDescription = metaDescription.trim();
    }
  }
}

