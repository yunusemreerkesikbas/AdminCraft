package com.backend.presentation.dto.request;

import com.backend.domain.enums.EntryFieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateEntryFieldRequest(
        @NotBlank(message = "validation.entry.field.key.required") @Size(max = 50, message = "validation.entry.field.key.size") String fieldKey,

        @NotNull(message = "validation.entry.field.type.required") EntryFieldType fieldType,

        Boolean isRequired,
        Integer maxLength,
        BigDecimal minValue,
        BigDecimal maxValue) {
}
