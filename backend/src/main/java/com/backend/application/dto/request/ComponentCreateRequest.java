package com.backend.application.dto.request;

import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.NavigationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import static com.backend.shared.constants.ValidationConstants.COMPONENT_NAME_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.COMPONENT_STYLE_CLASSES_MAX_LENGTH;

public record ComponentCreateRequest(
        @NotNull(message = "{validation.component.type.id.required}") Long componentTypeId,

        @NotBlank(message = "{validation.component.name.required}")
        @Size(max = COMPONENT_NAME_MAX_LENGTH, message = "{validation.component.name.size}")
        String name,

        Integer displayOrder,

        Boolean isVisible,

        @Size(max = COMPONENT_STYLE_CLASSES_MAX_LENGTH, message = "{validation.component.style.classes.size}")
        String styleClasses,

        Long navigationNodeId,

        Long navigationLinkNodeId,

        NavigationType navigationType,

        Boolean searchBox,

        ComponentStatus status) {
    public ComponentCreateRequest {
        if (name != null) {
            name = name.trim();
        }
    }
}
