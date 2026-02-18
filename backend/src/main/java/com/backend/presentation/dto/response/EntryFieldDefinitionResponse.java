package com.backend.presentation.dto.response;

import com.backend.application.dto.response.EntryFieldDefinitionResult;
import com.backend.domain.enums.EntryFieldType;

import java.time.LocalDateTime;

public record EntryFieldDefinitionResponse(
    Long id,
    Long componentTypeId,
    String fieldKey,
    EntryFieldType fieldType,
    LocalDateTime createdAt
) {
    public static EntryFieldDefinitionResponse from(EntryFieldDefinitionResult result) {
        return new EntryFieldDefinitionResponse(
            result.id(),
            result.componentTypeId(),
            result.fieldKey(),
            result.fieldType(),
            result.createdAt()
        );
    }
}
