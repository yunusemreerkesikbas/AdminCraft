package com.backend.application.dto.delivery;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

/**
 * Wrapper for content slots to match Hybris format.
 * Hybris returns: { "contentSlots": { "contentSlot": [...] } }
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ContentSlotsWrapper(List<ContentSlotDeliveryResponse> contentSlot) {

    public static ContentSlotsWrapper of(List<ContentSlotDeliveryResponse> slots) {
        return new ContentSlotsWrapper(slots != null ? slots : List.of());
    }

    public static ContentSlotsWrapper empty() {
        return new ContentSlotsWrapper(List.of());
    }
}
