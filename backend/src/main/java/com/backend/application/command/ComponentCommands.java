package com.backend.application.command;

import com.backend.domain.enums.ComponentStatus;
import com.fasterxml.jackson.databind.JsonNode;

public class ComponentCommands {

    public record CreateComponentCommand(
        Long componentTypeId,
        String code,
        String name,
        JsonNode baseData,
        JsonNode extendedData,
        ComponentStatus status,
        Long userId
    ) {}

    public record UpdateComponentCommand(
        Long id,
        Long componentTypeId,
        String code,
        String name,
        JsonNode baseData,
        JsonNode extendedData,
        ComponentStatus status,
        Long userId
    ) {}

    public record DeleteComponentCommand(Long id) {}
}

