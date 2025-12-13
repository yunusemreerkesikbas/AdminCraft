package com.backend.application.dto.template;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSlotDto {
  private Long id;
  private String uuid;
  private String slotName;
  private String position;
  private Integer sortOrder;
  private Boolean isRequired;
  private Integer maxComponents;
  private List<String> allowedTypes;
}
