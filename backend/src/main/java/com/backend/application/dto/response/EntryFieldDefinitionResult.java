package com.backend.application.dto.response;

import com.backend.domain.entity.EntryFieldDefinition;
import com.backend.domain.enums.EntryFieldType;

import java.time.LocalDateTime;

public record EntryFieldDefinitionResult(
    Long id,
    Long componentTypeId,
    String fieldKey,
    EntryFieldType fieldType,
    LocalDateTime createdAt
) {
    public static EntryFieldDefinitionResult from(EntryFieldDefinition entity) {
        return new EntryFieldDefinitionResult(
            entity.getId(),
            entity.getComponentTypeId(),
            entity.getFieldKey(),
            entity.getFieldType(),
            entity.getCreatedAt()
        );
    }
}
