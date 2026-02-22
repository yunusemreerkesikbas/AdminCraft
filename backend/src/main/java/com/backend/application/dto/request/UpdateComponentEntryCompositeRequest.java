package com.backend.application.dto.request;

import java.util.Map;

import com.backend.domain.enums.Language;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

@Builder
public record UpdateComponentEntryCompositeRequest(
    Integer sortOrder,
    Boolean isVisible,
    String styleClasses,
    Long responsiveMediaId,

    @NotEmpty(message = "At least one translation is required") @Valid Map<Language, EntryI18nRequest> translations) {
}
