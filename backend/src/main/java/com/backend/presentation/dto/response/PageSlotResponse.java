package com.backend.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageSlotResponse {

  private Long id;
  private String uid;
  private String slotName;
  private String position;
  private Integer sortOrder;
  private Boolean isActive;
  private Boolean isShared;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  private List<SlotComponentResponse> components;
}
