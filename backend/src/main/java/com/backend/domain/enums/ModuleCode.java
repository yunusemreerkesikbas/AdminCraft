package com.backend.domain.enums;

public enum ModuleCode {
    CORE("core", "Çekirdek Sistem", "Core System", true),
    PAGEBUILDER("pagebuilder", "Sayfa Oluşturucu", "Page Builder", false),
    PAGE_CATEGORIES("page_categories", "Sayfa Kategorileri", "Page Categories", false),
    SITE_SETTINGS("site_settings", "Site Ayarları", "Site Settings", false);

    private final String code;
    private final String displayNameTr;
    private final String displayNameEn;
    private final boolean required;

    ModuleCode(String code, String displayNameTr, String displayNameEn, boolean required) {
        this.code = code;
        this.displayNameTr = displayNameTr;
        this.displayNameEn = displayNameEn;
        this.required = required;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName(Language language) {
        return switch (language) {
            case TR -> displayNameTr;
            case EN -> displayNameEn;
            default -> displayNameTr;
        };
    }

    public String getDisplayNameTr() {
        return displayNameTr;
    }

    public String getDisplayNameEn() {
        return displayNameEn;
    }

    public boolean isRequired() {
        return required;
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
