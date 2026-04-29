package com.backend.application.dto.request;

import java.util.Map;

import com.backend.domain.enums.Language;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateComponentEntryCompositeRequest(
    Integer sortOrder,
    Boolean isVisible,
    String styleClasses,
    Long responsiveMediaId,

    @Size(min = 1, message = "{validation.component.entry.translations.required}") @Valid Map<Language, EntryI18nUpdateCommand> translations) {
}
