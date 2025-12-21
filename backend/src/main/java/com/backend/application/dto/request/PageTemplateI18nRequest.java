package com.backend.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PageTemplateI18nRequest(
    @NotBlank(message = "validation.name.required")
    @Size(max = 100, message = "validation.name.size")
    String name,

    @Size(max = 500, message = "validation.description.size")
    String description) {

  public PageTemplateI18nRequest {
    if (name != null) {
      name = name.trim();
    }
    if (description != null) {
      description = description.trim();
    }
  }
}
