package com.backend.application.command;

import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
import com.fasterxml.jackson.databind.JsonNode;

public class ComponentI18nCommands {

    public record UpsertComponentI18nCommand(
        Long componentId,
        Language language,
        JsonNode baseLocalizedData,
        ComponentStatus status
    ) {}

    public record PublishComponentI18nCommand(
        Long componentId,
        Language language
    ) {}

    public record UnpublishComponentI18nCommand(
        Long componentId,
        Language language
    ) {}

    public record DeleteComponentI18nCommand(
        Long componentId,
        Language language
    ) {}
}

