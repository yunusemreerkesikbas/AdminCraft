package com.backend.application.dto.response;

import java.util.List;

import com.backend.domain.enums.NodePosition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationNodeResponse {
  private Long id;
  private String uuid;
  private String uid;
  private String title;
  private Long parentId;
  private NodePosition position;
  private Integer sortOrder;
  private Boolean isVisible;
  private Boolean isTab;
  private List<NavigationEntryResponse> entries;
  private List<NavigationNodeResponse> children;
}
