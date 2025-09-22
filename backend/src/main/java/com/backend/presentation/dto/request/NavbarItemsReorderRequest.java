package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record NavbarItemsReorderRequest(
    @NotEmpty List<Change> changes) {
  public static record Change(
      Long itemId,
      Long parentId,
      Integer sortOrder) {
  }
}

