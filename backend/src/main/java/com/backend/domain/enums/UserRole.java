package com.backend.domain.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    SUPER_ADMIN,
    TENANT_ADMIN,
    VIEWER;

    public boolean hasPermission(Permission permission) {
        return switch (this) {
            case SUPER_ADMIN -> true;
            case TENANT_ADMIN -> switch (permission) {
                case READ_CONTENT, WRITE_CONTENT, DELETE_CONTENT,
                        PUBLISH_CONTENT,
                        READ_USER, WRITE_USER, DELETE_USER,
                        READ_MEDIA, WRITE_MEDIA, DELETE_MEDIA,
                        MANAGE_TENANT ->
                    true;
                default -> false;
            };
            case VIEWER -> switch (permission) {
                case READ_CONTENT, READ_MEDIA, READ_USER -> true;
                default -> false;
            };
        };
    }

    public enum Permission {
        READ_CONTENT, WRITE_CONTENT, DELETE_CONTENT,
        READ_USER, WRITE_USER, DELETE_USER,
        READ_MEDIA, WRITE_MEDIA, DELETE_MEDIA,
        MANAGE_TENANT, PUBLISH_CONTENT
    }
}
