package com.backend.presentation.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ReorderSlotComponentsRequest {

  @NotEmpty(message = "Component IDs list cannot be empty")
  private List<Long> componentIds;
}
