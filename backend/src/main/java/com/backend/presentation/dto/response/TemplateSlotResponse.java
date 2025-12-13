package com.backend.presentation.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSlotResponse {
  private Long id;
  private String uuid;
  private String slotName;
  private String position;
  private Integer sortOrder;
  private Boolean isRequired;
  private Integer maxComponents;
  private List<String> allowedTypes;
}
