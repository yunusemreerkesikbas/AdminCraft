package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePageSlotRequest {

  @NotBlank(message = "Slot name is required")
  @Size(max = 50, message = "Slot name must not exceed 50 characters")
  private String slotName;

  @NotBlank(message = "Position is required")
  @Size(max = 20, message = "Position must not exceed 20 characters")
  private String position;

  private Integer sortOrder = 0;

  private Boolean isShared = false;
}
