package com.backend.application.dto.request;

import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.NavigationType;
import com.backend.shared.validation.Uid;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import static com.backend.shared.constants.ValidationConstants.COMPONENT_NAME_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.COMPONENT_STYLE_CLASSES_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.UID_TEMPLATE_MAX_LENGTH;

public record ComponentCreateRequest(
        @NotNull(message = "{validation.component.type.id.required}") Long componentTypeId,

        @Uid(maxLength = UID_TEMPLATE_MAX_LENGTH) String uid,

        @NotBlank(message = "{validation.component.name.required}")
        @Size(max = COMPONENT_NAME_MAX_LENGTH, message = "{validation.component.name.size}")
        String name,

        Integer displayOrder,

        Boolean isVisible,

        @Size(max = COMPONENT_STYLE_CLASSES_MAX_LENGTH, message = "{validation.component.style.classes.size}")
        String styleClasses,

        Long navigationNodeId,

        NavigationType navigationType,

        Boolean searchBox,

        ComponentStatus status) {
    public ComponentCreateRequest {
        if (uid != null) {
            uid = uid.trim();
        }
        if (name != null) {
            name = name.trim();
        }
    }
}
