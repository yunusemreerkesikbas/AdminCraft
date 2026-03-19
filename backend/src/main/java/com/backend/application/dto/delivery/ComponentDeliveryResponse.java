package com.backend.application.dto.delivery;

import java.util.List;
import java.util.Map;

import com.backend.domain.enums.NavigationType;
public record ComponentDeliveryResponse(
    String uid,
    String type,
    String category,
    String title,
    String subtitle,
    String description,
    Boolean isVisible,
    String styleClasses,
    Map<String, Object> customFields,
    NavigationType navigationType,
    Boolean searchBox,
    NavigationDeliveryResponse navigationNode,
    ResponsiveMediaDeliveryResponse responsive,
    List<EntryDeliveryResponse> entries) {
}
