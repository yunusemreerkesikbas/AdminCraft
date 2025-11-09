package com.backend.domain.enums;

public enum ModuleCode {
    CORE("core", "Core"),
    PAGEBUILDER("pagebuilder", "Page Builder"),
    SITE_SETTINGS("site_settings", "Site Settings"),
    MEDIA("media", "Media Library"),
    COMPONENT_LIBRARY("component_library", "Component Library");

    private final String code;
    private final String name;

    ModuleCode(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static ModuleCode fromCode(String code) {
        for (ModuleCode module : values()) {
            if (module.code.equalsIgnoreCase(code)) {
                return module;
            }
        }
        throw new IllegalArgumentException("Unsupported module code: " + code);
    }

    public static boolean isValidCode(String code) {
        try {
            fromCode(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
