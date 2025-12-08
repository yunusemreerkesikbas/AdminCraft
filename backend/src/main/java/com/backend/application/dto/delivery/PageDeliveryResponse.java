package com.backend.application.dto.delivery;

import java.util.List;
import java.util.Map;

import lombok.Builder;

@Builder
public record PageDeliveryResponse(
    String uid,
    String title,
    String subtitle,
    String description,
    String metaTitle,
    String metaDescription,
    String robotTag,
    String urlPath,
    String featuredImage,
    String styleClasses,
    Map<String, List<ComponentDeliveryResponse>> slots) {
}
