package com.backend.application.dto.request;

import java.util.Map;

import com.backend.domain.enums.Language;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateComponentEntryCompositeRequest(
    @NotNull(message = "{validation.component.id.required}") Long componentId,

    Integer sortOrder,
    Boolean isVisible,
    String styleClasses,
    Long responsiveMediaId,

    @NotEmpty(message = "{validation.component.entry.translations.required}") @Valid Map<Language, EntryI18nRequest> translations) {
}
