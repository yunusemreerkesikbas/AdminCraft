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
    String labelTr,
    String labelEn,
    Boolean isRequired,
    Integer maxLength,
    BigDecimal minValue,
    BigDecimal maxValue,
    LocalDateTime createdAt
) {
    public static EntryFieldDefinitionResponse from(EntryFieldDefinition entity) {
        return new EntryFieldDefinitionResponse(
            entity.getId(),
            entity.getComponentTypeId(),
            entity.getFieldKey(),
            entity.getFieldType(),
            entity.getLabelTr(),
            entity.getLabelEn(),
            entity.getIsRequired(),
            entity.getMaxLength(),
            entity.getMinValue(),
            entity.getMaxValue(),
            entity.getCreatedAt()
        );
    }
}



