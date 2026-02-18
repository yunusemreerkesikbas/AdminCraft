package com.backend.application.dto.request;

import java.util.Map;

import static com.backend.shared.constants.ValidationConstants.UID_TEMPLATE_MAX_LENGTH;

import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.NavigationType;
import com.backend.shared.validation.Uid;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateComponentCompositeRequest(
    @NotNull(message = "{validation.component.type.id.required}") Long componentTypeId,

    @Uid(maxLength = UID_TEMPLATE_MAX_LENGTH) String uid,

    @NotBlank(message = "{validation.component.name.required}") @Size(max = 100, message = "{validation.component.name.size}") String name,

    Integer displayOrder,

    Boolean isVisible,

    @Size(max = 500, message = "{validation.component.style.classes.size}") String styleClasses,

    Long navigationNodeId,

    Long navigationLinkNodeId,

    NavigationType navigationType,

    Boolean searchBox,

    ComponentStatus status,

    @NotEmpty(message = "{validation.component.translations.required}") @Valid Map<Language, ComponentI18nCommand> translations) {

  public CreateComponentCompositeRequest {
    uid = uid == null ? null : uid.trim();
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
