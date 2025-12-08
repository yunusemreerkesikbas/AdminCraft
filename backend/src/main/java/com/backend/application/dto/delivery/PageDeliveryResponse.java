package com.backend.application.dto.delivery;

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

    Map<String, java.util.List<ComponentDeliveryResponse>> slots) {
}
