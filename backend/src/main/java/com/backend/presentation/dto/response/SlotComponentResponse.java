package com.backend.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotComponentResponse {

  private Long id;
  private Long componentId;
  private String componentUid;
  private String componentName;
  private String componentTypeName;
  private Integer sortOrder;
  private Boolean isVisible;
}
