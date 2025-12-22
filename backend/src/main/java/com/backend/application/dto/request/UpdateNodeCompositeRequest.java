package com.backend.application.dto.request;

import java.util.Map;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.NodePosition;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record UpdateNodeCompositeRequest(
    NodePosition position,

    Boolean isVisible,

    Boolean isTab,

    @NotEmpty(message = "At least one translation is required")
    @Valid
    Map<Language, NavigationNodeI18nRequest> translations) {
}
