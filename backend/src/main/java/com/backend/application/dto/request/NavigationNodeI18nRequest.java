package com.backend.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NavigationNodeI18nRequest(
    @NotBlank(message = "validation.title.required") @Size(max = 200, message = "validation.title.size") String title) {

  public NavigationNodeI18nRequest {
    if (title != null) {
      title = title.trim();
    }
  }
}
