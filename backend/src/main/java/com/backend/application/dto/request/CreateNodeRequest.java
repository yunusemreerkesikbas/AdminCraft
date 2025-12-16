package com.backend.application.dto.request;

import com.backend.domain.enums.NodePosition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateNodeRequest(
    @NotBlank(message = "UID is required") @Size(max = 100, message = "UID must be at most 100 characters") String uid,

    @Size(max = 200, message = "Title must be at most 200 characters") String title,

    NodePosition position,

    Boolean isVisible,

    Boolean isTab) {
  public CreateNodeRequest {
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
