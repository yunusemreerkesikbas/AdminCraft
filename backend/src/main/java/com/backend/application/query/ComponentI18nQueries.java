package com.backend.application.query;

import com.backend.domain.enums.Language;

public class ComponentI18nQueries {

    public record GetComponentI18nQuery(
        Long componentId,
        Language language
    ) {}

    public record GetComponentI18nByComponentIdQuery(Long componentId) {}
}

