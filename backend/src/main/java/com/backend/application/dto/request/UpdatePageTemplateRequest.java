package com.backend.application.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.backend.shared.constants.ValidationConstants.PAGE_TEMPLATE_DESCRIPTION_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.PAGE_TEMPLATE_NAME_MAX_LENGTH;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePageTemplateRequest {

  @Size(max = PAGE_TEMPLATE_NAME_MAX_LENGTH, message = "validation.page.template.name.size")
  private String name;

  @Size(max = PAGE_TEMPLATE_DESCRIPTION_MAX_LENGTH, message = "validation.page.template.description.size")
  private String description;

  private Boolean isActive;
}
