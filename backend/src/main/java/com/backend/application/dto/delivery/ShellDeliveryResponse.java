package com.backend.application.dto.delivery;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(NON_NULL)
public record ShellDeliveryResponse(
    HeaderDelivery header,
    FooterDelivery footer
) {
  public record HeaderDelivery(
      NavigationDeliveryResponse mainNavigation,
      List<LayoutBlockDelivery> primaryBlocks,
      List<LayoutBlockDelivery> secondaryBlocks
  ) {}

  public record FooterDelivery(
      List<LayoutBlockDelivery> primaryBlocks,
      List<LayoutBlockDelivery> bottomBlocks
  ) {}

  @JsonInclude(NON_NULL)
  public record LayoutBlockDelivery(
      String uid,
      String role,
      String componentType,
      String title,
      String description,
      NavigationDeliveryResponse navigationNode,
      List<LayoutLinkDeliveryDto> links,
      String newsletterPlaceholder,
      String newsletterButtonLabel
  ) {}

}
