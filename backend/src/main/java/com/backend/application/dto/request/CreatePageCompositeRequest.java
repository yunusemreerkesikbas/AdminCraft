package com.backend.application.dto.request;

import java.util.Map;

import com.backend.domain.enums.PageStatus;
import com.backend.domain.enums.RobotTag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * Composite request for creating a Page with all translations in a single
 * transaction.
 * Follows the pattern established in Sprint 34 for atomic i18n operations.
 * 
 * Note: translations map uses String keys (e.g., "TR", "EN") for JSON
 * compatibility.
 * Service layer converts these to Language enum.
 */
public record CreatePageCompositeRequest(
    Long templateId,

    PageStatus status,

    @Size(max = 255, message = "validation.style.classes.size") String styleClasses,

    RobotTag robotTag,

    @NotEmpty(message = "validation.translations.required") @Valid Map<String, PageI18nCompositeRequest> translations,
    @Size(max = 36) String uid) {

  public CreatePageCompositeRequest {
    if (status == null) {
      status = PageStatus.DRAFT;
    }
    if (styleClasses != null) {
      styleClasses = styleClasses.trim();
    }
  }
}
