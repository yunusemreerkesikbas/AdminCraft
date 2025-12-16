package com.backend.application.dto.delivery;

import java.util.List;

import com.backend.domain.enums.NavigationItemType;
import com.backend.domain.enums.NodePosition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationDeliveryResponse {
  private String uid;
  private String title;
  private NodePosition position;
  private Boolean isTab;
  private List<EntryDeliveryDto> entries;
  private List<NavigationDeliveryResponse> children;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class EntryDeliveryDto {
    private String uid;
    private NavigationItemType itemType;
    private String itemId;
    private String url;
    private String linkName;
    private String linkColor;
    private String target;
    private Boolean isExternal;
  }
}
