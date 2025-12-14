package com.backend.presentation.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReorderTemplateSlotsRequest {
  @NotEmpty(message = "Slot names list cannot be empty")
  private List<String> slotNames;
}
