package com.backend.domain.enums;

public enum TwoFactorPolicy {
    DISABLED("disabled", "Devre Dışı", "Disabled"),
    OPTIONAL("optional", "İsteğe Bağlı", "Optional"),
    REQUIRED("required", "Zorunlu", "Required");

    private final String code;
    private final String displayNameTr;
    private final String displayNameEn;

    TwoFactorPolicy(String code, String displayNameTr, String displayNameEn) {
        this.code = code;
        this.displayNameTr = displayNameTr;
        this.displayNameEn = displayNameEn;
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

    public boolean requiresOtp() {
        return this == REQUIRED;
    }

    public boolean supportsOtp() {
        return this != DISABLED;
    }
}
