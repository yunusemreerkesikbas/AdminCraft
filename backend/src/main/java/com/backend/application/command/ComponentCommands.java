package com.backend.application.command;

import com.backend.domain.enums.ComponentStatus;

public class ComponentCommands {

    public record CreateComponentCommand(
            Long componentTypeId,
            String name,
            Integer displayOrder,
            Boolean isVisible,
            String styleClasses,
            ComponentStatus status,
            Long userId) {
    }

    public record UpdateComponentCommand(
            Long id,
            Long componentTypeId,
            String name,
            Integer displayOrder,
            Boolean isVisible,
            String styleClasses,
            ComponentStatus status,
            Long userId) {
    }

    public record DeleteComponentCommand(Long id) {
    }
}
