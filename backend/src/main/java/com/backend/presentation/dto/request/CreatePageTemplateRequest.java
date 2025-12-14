package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePageTemplateRequest {

  @NotBlank(message = "Name is required")
  @Size(max = 100, message = "Name must be at most 100 characters")
  private String name;

  @NotBlank(message = "UID is required")
  @Size(max = 50, message = "UID must be at most 50 characters")
  @Pattern(regexp = "^[a-z][a-z0-9-]*$", message = "UID must start with lowercase letter and contain only lowercase letters, numbers, and hyphens")
  private String uid;

  @Size(max = 500, message = "Description must be at most 500 characters")
  private String description;

  private Boolean isActive = true;
}
