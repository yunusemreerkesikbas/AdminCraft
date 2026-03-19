package com.backend.application.dto.delivery;

import java.util.List;

import com.backend.application.dto.delivery.ShellDeliveryResponse.LayoutLinkDelivery;
import com.backend.domain.enums.NavigationItemType;
import com.backend.domain.enums.NodePosition;

import com.fasterxml.jackson.annotation.JsonInclude;
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
  private List<NavigationSectionDelivery> sections; // null = regular nav endpoint
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<LayoutLinkDelivery> flatLinks;

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
    private boolean isExternal;
    private String resolvedHref;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class NavigationSectionDelivery {
    private String uid;
    private String title;
    private String href;
    private boolean isExternal;
    private List<LayoutLinkDelivery> links;
  }
}
