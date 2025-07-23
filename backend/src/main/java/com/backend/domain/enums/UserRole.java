package com.backend.domain.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    SUPER_ADMIN("SUPER_ADMIN", "user.role.super.admin", "Süper Yönetici", "Super Administrator"),
    TENANT_ADMIN("TENANT_ADMIN", "user.role.tenant.admin", "Kiracı Yöneticisi", "Tenant Administrator"),
    EDITOR("EDITOR", "user.role.editor", "Editör", "Editor"),
    VIEWER("VIEWER", "user.role.viewer", "Görüntüleyici", "Viewer");

    private final String code;
    private final String messageKey;
    private final String displayNameTr;
    private final String displayNameEn;

    UserRole(String code, String messageKey, String displayNameTr, String displayNameEn) {
        this.code = code;
        this.messageKey = messageKey;
        this.displayNameTr = displayNameTr;
        this.displayNameEn = displayNameEn;
    }

    public String getDisplayName(Language language) {
        return switch (language) {
            case TR -> displayNameTr;
            case EN -> displayNameEn;
            default -> displayNameTr; // fallback to Turkish
        };
    }

    public boolean hasPermission(Permission permission) {
        return switch (this) {
            case SUPER_ADMIN -> true; // Super admin has all permissions
            case TENANT_ADMIN -> switch (permission) {
                case READ_CONTENT, WRITE_CONTENT, DELETE_CONTENT, 
                     READ_USER, WRITE_USER, DELETE_USER,
                     READ_MEDIA, WRITE_MEDIA, DELETE_MEDIA,
                     MANAGE_TENANT -> true;
                default -> false;
            };
            case EDITOR -> switch (permission) {
                case READ_CONTENT, WRITE_CONTENT,
                     READ_MEDIA, WRITE_MEDIA,
                     READ_USER -> true;
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