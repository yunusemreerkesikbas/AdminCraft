package com.backend.application.dto.request;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
  @Pattern(regexp = "^[A-Z][a-zA-Z0-9]*$", message = "Slot name must be PascalCase (e.g., Header, MainContent)")
  private String slotName;

  @NotBlank(message = "Position is required")
  @Size(max = 20, message = "Position must be at most 20 characters")
  @Pattern(regexp = "^(TOP|CENTER|BOTTOM|LEFT|RIGHT)$", message = "Position must be one of: TOP, CENTER, BOTTOM, LEFT, RIGHT")
  private String position;

  @Min(value = 0, message = "Sort order cannot be negative")
  private Integer sortOrder = 0;

  private Boolean isRequired = false;

  @Min(value = 1, message = "Max components must be at least 1")
  private Integer maxComponents;

  @Size(max = 50, message = "Maximum 50 allowed types")
  private List<@NotBlank @Size(max = 100) String> allowedTypes;
}
