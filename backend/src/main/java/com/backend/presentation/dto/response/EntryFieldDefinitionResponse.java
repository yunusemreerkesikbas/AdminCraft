package com.backend.presentation.dto.response;

import com.backend.domain.entity.EntryFieldDefinition;
import com.backend.domain.enums.EntryFieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EntryFieldDefinitionResponse(
    Long id,
    Long componentTypeId,
    String fieldKey,
    EntryFieldType fieldType,
    Boolean isRequired,
    Integer maxLength,
    BigDecimal minValue,
    BigDecimal maxValue,
    String migrationVersion,
    LocalDateTime appliedAt,
    LocalDateTime createdAt
) {
    public static EntryFieldDefinitionResponse from(EntryFieldDefinition entity) {
        return new EntryFieldDefinitionResponse(
            entity.getId(),
            entity.getComponentTypeId(),
            entity.getFieldKey(),
            entity.getFieldType(),
            entity.getIsRequired(),
            entity.getMaxLength(),
            entity.getMinValue(),
            entity.getMaxValue(),
            entity.getMigrationVersion(),
            entity.getAppliedAt(),
            entity.getCreatedAt()
        );
    }
}



