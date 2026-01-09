package com.backend.application.dto.delivery;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

/**
 * Page delivery response with Hybris-compatible format.
 *
 * Supports both formats:
 * - Legacy: slots as Map<String, List<ComponentDeliveryResponse>>
 * - Hybris: contentSlots with structured slot metadata
 */
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
    ContentSlotsWrapper contentSlots,

    // Legacy format (kept for backwards compatibility)
    Map<String, List<ComponentDeliveryResponse>> slots) {

    /**
     * Builder helper for creating Hybris-compatible response.
     */
    public static PageDeliveryResponseBuilder hybrisBuilder() {
        return builder().typeCode("ContentPage");
    }
}
