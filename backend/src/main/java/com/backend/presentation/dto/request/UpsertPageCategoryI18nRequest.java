package com.backend.presentation.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpsertPageCategoryI18nRequest(
    @Size(max = 150, message = "URL must not exceed 150 characters") @Pattern(regexp = "^[a-z0-9-]*$", message = "URL can only contain lowercase letters, numbers and dashes") String url,

    @Size(max = 200, message = "Title must not exceed 200 characters") String title,

    @Size(max = 60, message = "Meta title must not exceed 60 characters") String metaTitle,

    @Size(max = 160, message = "Meta description must not exceed 160 characters") String metaDescription,

    Boolean active) {
  public UpsertPageCategoryI18nRequest {
    if (url != null) {
      url = url.toLowerCase().trim();
      if (url.contains("..") || url.contains("./") || url.contains("\\")) {
        throw new IllegalArgumentException("Invalid URL format - security violation");
      }
    }

    if (title != null) {
      title = title.trim();
      if (title.contains("<") || title.contains(">")) {
        throw new IllegalArgumentException("Title cannot contain HTML tags");
      }
    }

    if (active == null) {
      active = true;
    }
  }
}
