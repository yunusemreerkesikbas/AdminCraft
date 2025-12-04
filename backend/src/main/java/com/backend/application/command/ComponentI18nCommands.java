package com.backend.application.command;

import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;

public class ComponentI18nCommands {

    public record UpsertComponentI18nCommand(
        Long componentId,
        Language language,
        String title,
        String subtitle,
        String description,
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

