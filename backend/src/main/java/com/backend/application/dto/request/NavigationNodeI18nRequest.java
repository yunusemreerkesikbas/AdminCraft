package com.backend.application.dto.request;

import jakarta.validation.constraints.Size;

public record NavigationNodeI18nRequest(
    @Size(max = 200, message = "validation.title.size")
    String title) {

  public NavigationNodeI18nRequest {
    if (title != null) {
      title = title.trim();
    }
  }
}
