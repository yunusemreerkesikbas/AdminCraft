package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddComponentToSlotRequest {

  @NotNull(message = "Component ID is required")
  private Long componentId;
}
