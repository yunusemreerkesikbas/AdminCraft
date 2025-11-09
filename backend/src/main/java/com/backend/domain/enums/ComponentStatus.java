package com.backend.domain.enums;

import lombok.Getter;

@Getter
public enum ComponentStatus {
    DRAFT("DRAFT", "Draft"),
    ACTIVE("ACTIVE", "Active"),
    INACTIVE("INACTIVE", "Inactive");

    private final String code;
    private final String name;

    ComponentStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public boolean canTransitionTo(ComponentStatus newStatus) {
        return switch (this) {
            case DRAFT -> newStatus == ACTIVE || newStatus == INACTIVE;
            case ACTIVE -> newStatus == DRAFT || newStatus == INACTIVE;
            case INACTIVE -> newStatus == DRAFT || newStatus == ACTIVE;
        };
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean canBeEdited() {
        return this == DRAFT || this == INACTIVE;
    }
}
