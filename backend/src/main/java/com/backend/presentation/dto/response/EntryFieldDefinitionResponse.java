package com.backend.presentation.dto.response;

import com.backend.domain.entity.EntryFieldDefinition;
import com.backend.domain.enums.EntryFieldType;

import java.time.LocalDateTime;

public record EntryFieldDefinitionResponse(
    Long id,
    Long componentTypeId,
    String fieldKey,
    EntryFieldType fieldType,
    LocalDateTime createdAt
) {
    public static EntryFieldDefinitionResponse from(EntryFieldDefinition entity) {
        return new EntryFieldDefinitionResponse(
            entity.getId(),
            entity.getComponentTypeId(),
            entity.getFieldKey(),
            entity.getFieldType(),
            entity.getCreatedAt()
        );
    }
}


