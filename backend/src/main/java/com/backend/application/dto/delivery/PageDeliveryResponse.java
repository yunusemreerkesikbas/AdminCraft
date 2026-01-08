package com.backend.application.dto.delivery;

import java.util.List;
import java.util.Map;

import lombok.Builder;

@Builder
public record PageDeliveryResponse(
    String uid,
    String name,
    String title,
    String description,
    String robotTag,
    String canonicalUrl,
    String styleClasses,
    Map<String, List<ComponentDeliveryResponse>> slots) {
}
