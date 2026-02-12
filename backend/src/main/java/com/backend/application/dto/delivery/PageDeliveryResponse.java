package com.backend.application.dto.delivery;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PageDeliveryResponse(
    String uid,
    String name,
    String title,
    String description,
    String robotTag,
    String canonicalUrl,
    String styleClasses,

    // Hybris-compatible fields
    String template,
    String typeCode,
    String code,
    ContentSlotsWrapper contentSlots,

    // Legacy format
    Map<String, List<ComponentDeliveryResponse>> slots) {
}
