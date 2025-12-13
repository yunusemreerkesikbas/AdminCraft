package com.backend.presentation.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePageTemplateRequest {

  @Size(max = 100, message = "Name must be at most 100 characters")
  private String name;

  @Size(max = 500, message = "Description must be at most 500 characters")
  private String description;

  private Boolean isActive;
}
