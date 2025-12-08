package com.backend.application.dto.slot;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

@Builder
public record PageSlotDto(
    Long id,
    String uid,
    String slotName,
    String position,
    Integer sortOrder,
    Boolean isActive,
    Boolean isShared,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<SlotComponentDto> components) {
}

