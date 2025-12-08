package com.backend.application.dto.slot;

import lombok.Builder;

@Builder
public record SlotComponentDto(
    Long id,
    Long componentId,
    String componentUid,
    String componentName,
    String componentTypeName,
    Integer sortOrder,
    Boolean isVisible) {
}

