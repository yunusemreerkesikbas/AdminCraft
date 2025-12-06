package com.backend.presentation.dto.response.delivery;

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
    List<EntryDeliveryResponse> entries) {
}
