package com.backend.presentation.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTemplateSlotRequest {

  @NotBlank(message = "Slot name is required")
  @Size(max = 50, message = "Slot name must be at most 50 characters")
  private String slotName;

  @NotBlank(message = "Position is required")
  @Size(max = 20, message = "Position must be at most 20 characters")
  private String position;

  private Integer sortOrder = 0;

  private Boolean isRequired = false;

  private Integer maxComponents;

  private List<String> allowedTypes;
}
