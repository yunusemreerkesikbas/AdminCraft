package com.backend.application.dto.response;

import com.backend.domain.enums.NavigationItemType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationEntryResponse {
  private Long id;
  private String uuid;
  private String uid;
  private Long nodeId;
  private NavigationItemType itemType;
  private String itemId;
  private String url;
  private String linkName;
  private String linkColor;
  private String target;
  private Boolean isExternal;
  private Integer sortOrder;
  private Boolean isVisible;
}
