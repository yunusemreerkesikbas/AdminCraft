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
public class PageTemplateDto {
  private Long id;
  private String uuid;
  private String uid;
  private String name;
  private String description;
  private Boolean isSystem;
  private Boolean isActive;
  private List<TemplateSlotDto> slots;
}
