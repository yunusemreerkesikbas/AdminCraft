package com.backend.application.dto.request;

import static com.backend.shared.constants.ValidationConstants.COMPONENT_NAME_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.COMPONENT_STYLE_CLASSES_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.UID_TEMPLATE_MAX_LENGTH;

import java.util.Map;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.NavigationType;
import com.backend.shared.validation.Uid;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

public record UpdateComponentCompositeRequest(
        @Uid(maxLength = UID_TEMPLATE_MAX_LENGTH) String uid,

        @Size(max = COMPONENT_NAME_MAX_LENGTH, message = "{validation.component.name.size}") String name,

        Integer displayOrder,

        Boolean isVisible,

        @Size(max = COMPONENT_STYLE_CLASSES_MAX_LENGTH, message = "{validation.component.style.classes.size}") String styleClasses,

        Long navigationNodeId,

        NavigationType navigationType,

        Boolean searchBox,

        Long responsiveMediaId,

        @Valid Map<Language, ComponentI18nCommand> translations) {

    public UpdateComponentCompositeRequest {
        uid = uid == null ? null : uid.trim();
    }
}
