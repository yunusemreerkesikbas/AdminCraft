package com.backend.application.command;

public class ComponentTypeCommands {

    public record CreateComponentTypeCommand(
        String code,
        String name,
        String category,
        String icon,
        Long userId
    ) {}

    public record UpdateComponentTypeCommand(
        Long id,
        String code,
        String name,
        String category,
        String icon,
        Long userId
    ) {}

    public record DeleteComponentTypeCommand(Long id) {}
}

