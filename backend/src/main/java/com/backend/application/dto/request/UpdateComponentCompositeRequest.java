package com.backend.application.dto.request;

import java.util.Map;

import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UpdateComponentCompositeRequest(
        @Size(max = 100, message = "Name must be at most 100 characters") String name,

        Integer displayOrder,

        Boolean isVisible,

        @Size(max = 500, message = "Style classes must be at most 500 characters") String styleClasses,

        ComponentStatus status,

        @NotEmpty(message = "At least one translation is required") @Valid Map<Language, ComponentI18nCommand> translations) {
}
