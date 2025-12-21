package com.backend.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NavigationEntryI18nRequest(
    @NotBlank(message = "validation.linkName.required")
    @Size(max = 200, message = "validation.linkName.size")
    String linkName) {

  public NavigationEntryI18nRequest {
    if (linkName != null) {
      linkName = linkName.trim();
    }
  }
}
