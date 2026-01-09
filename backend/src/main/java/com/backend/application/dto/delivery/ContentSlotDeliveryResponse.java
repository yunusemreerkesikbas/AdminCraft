package com.backend.application.dto.delivery;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

/**
 * Hybris-compatible content slot delivery response.
 * Maps to SAP Commerce Cloud's ContentSlotForPage/ContentSlotForTemplate structure.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ContentSlotDeliveryResponse(
    String slotId,
    String slotUuid,
    String position,
    String name,
    boolean slotShared,
    ComponentsWrapper components) {

    /**
     * Wrapper for components list to match Hybris format.
     * Hybris returns: { "component": [...] }
     */
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ComponentsWrapper(List<ComponentDeliveryResponse> component) {
        public static ComponentsWrapper of(List<ComponentDeliveryResponse> components) {
            return new ComponentsWrapper(components != null ? components : List.of());
        }
    }
}
