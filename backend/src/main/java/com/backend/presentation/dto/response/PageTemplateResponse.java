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
public class PageTemplateResponse {
  private Long id;
  private String uuid;
  private String uid;
  private String name;
  private String description;
  private Boolean isSystem;
  private Boolean isActive;
  private List<TemplateSlotResponse> slots;
}
