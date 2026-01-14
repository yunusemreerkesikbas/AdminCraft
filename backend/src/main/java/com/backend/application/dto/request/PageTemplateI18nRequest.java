package com.backend.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.backend.shared.constants.ValidationConstants.PAGE_TEMPLATE_DESCRIPTION_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.PAGE_TEMPLATE_NAME_MAX_LENGTH;

public record PageTemplateI18nRequest(
  @NotBlank(message = "validation.page.template.name.required")
  @Size(max = PAGE_TEMPLATE_NAME_MAX_LENGTH, message = "validation.page.template.name.size")
    String name,

  @Size(max = PAGE_TEMPLATE_DESCRIPTION_MAX_LENGTH, message = "validation.page.template.description.size")
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
