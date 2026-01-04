package com.backend.application.dto.delivery;

import java.util.List;

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
    ResponsiveMediaDeliveryResponse responsive,
    List<EntryDeliveryResponse> entries) {
}
