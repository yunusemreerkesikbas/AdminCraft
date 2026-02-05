package com.backend.domain.enums;

public enum TokenStatus {
    ACTIVE("active", "Aktif", "Active"),
    USED("used", "Kullanıldı", "Used"),
    EXPIRED("expired", "Süresi Doldu", "Expired"),
    REVOKED("revoked", "İptal Edildi", "Revoked");

    private final String code;
    private final String displayNameTr;
    private final String displayNameEn;

    TokenStatus(String code, String displayNameTr, String displayNameEn) {
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

    public boolean isUsable() {
        return this == ACTIVE;
    }
}
