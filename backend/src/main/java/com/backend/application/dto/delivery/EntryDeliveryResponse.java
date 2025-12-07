package com.backend.application.dto.delivery;

import java.util.Map;

import lombok.Builder;

@Builder
public record EntryDeliveryResponse(
    String uid,
    Integer order,
    String title,
    String description,
    Boolean isVisible,
    String styleClasses,
    Map<String, Object> customFields) {
}
