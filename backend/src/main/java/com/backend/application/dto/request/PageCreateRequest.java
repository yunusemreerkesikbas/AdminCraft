package com.backend.application.dto.request;

import com.backend.domain.enums.PageStatus;
import com.backend.domain.enums.RobotTag;

import jakarta.validation.constraints.Size;

public record PageCreateRequest(
    Long templateId,
    PageStatus status,
    @Size(max = 255, message = "validation.style.classes.size") String styleClasses,
    RobotTag robotTag,
    @Size(max = 36) String uid) {

  public PageCreateRequest {
    if (styleClasses != null) {
      styleClasses = styleClasses.trim();
    }
  }
}
