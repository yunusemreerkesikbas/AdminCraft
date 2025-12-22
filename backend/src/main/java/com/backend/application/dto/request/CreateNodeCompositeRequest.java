package com.backend.application.dto.request;

import java.util.Map;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.NodePosition;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record CreateNodeCompositeRequest(
    @NotBlank(message = "UID is required")
    @Size(max = 100, message = "UID must be at most 100 characters")
    String uid,

    NodePosition position,

    Boolean isVisible,

    Boolean isTab,

    Long parentId,

    @NotEmpty(message = "At least one translation is required")
    @Valid
    Map<Language, NavigationNodeI18nRequest> translations) {

  public CreateNodeCompositeRequest {
    if (position == null) {
      position = NodePosition.LEFT;
    }
    if (isVisible == null) {
      isVisible = true;
    }
    if (isTab == null) {
      isTab = false;
    }
  }
}
