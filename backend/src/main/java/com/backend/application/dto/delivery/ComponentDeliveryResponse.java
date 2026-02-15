package com.backend.application.dto.delivery;

import java.util.List;

import com.backend.domain.enums.NavigationType;

import lombok.Builder;

@Builder
public record ComponentDeliveryResponse(
    String uid,
    String type,
    String category,
    String title,
    String subtitle,
    String description,
    Boolean isVisible,
    String styleClasses,
    NavigationType navigationType,
    Boolean searchBox,
    NavigationDeliveryResponse navigationNode,
    NavigationDeliveryResponse navigationLinkNode,
    ResponsiveMediaDeliveryResponse responsive,
    List<EntryDeliveryResponse> entries) {
}
