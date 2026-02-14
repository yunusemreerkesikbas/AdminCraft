package com.backend.application.dto.request;

import java.util.Map;

import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateComponentCompositeRequest(
    @NotNull(message = "validation.component.type.id.required") Long componentTypeId,

    @NotBlank(message = "validation.component.name.required") @Size(max = 100, message = "validation.component.name.size") String name,

    Integer displayOrder,

    Boolean isVisible,

    @Size(max = 500, message = "validation.component.style.classes.size") String styleClasses,

    Long navigationNodeId,

    Long navigationLinkNodeId,

    @Size(max = 50, message = "validation.component.navigation.type.size") String navigationType,

    Boolean searchBox,

    Integer wrapAfter,

    ComponentStatus status,

    @NotEmpty(message = "validation.component.translations.required") @Valid Map<Language, ComponentI18nCommand> translations) {

  public CreateComponentCompositeRequest {
    if (displayOrder == null) {
      displayOrder = 0;
    }
    if (isVisible == null) {
      isVisible = true;
    }
    if (status == null) {
      status = ComponentStatus.DRAFT;
    }
  }
}
