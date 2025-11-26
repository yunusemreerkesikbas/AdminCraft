package com.backend.application.dto.provisioning;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvisionRequest {

  @NotEmpty(message = "At least one module must be selected")
  private List<String> modules;
}

