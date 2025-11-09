package com.backend.application.command;

import com.fasterxml.jackson.databind.JsonNode;

public class ComponentTypeCommands {

    public record CreateComponentTypeCommand(
        String code,
        String name,
        String category,
        String icon,
        JsonNode extendedFieldsSchema,
        Long userId
    ) {}

    public record UpdateComponentTypeCommand(
        Long id,
        String code,
        String name,
        String category,
        String icon,
        JsonNode extendedFieldsSchema,
        Long userId
    ) {}

    public record DeleteComponentTypeCommand(Long id) {}
}

