package com.backend.domain.enums;

public enum TenantStatus {
    PENDING("pending", "Onay Bekliyor", "Pending Approval"),
    ACTIVE("active", "Aktif", "Active"),
    SUSPENDED("suspended", "Askıya Alındı", "Suspended"),
    MAINTENANCE("maintenance", "Bakım", "Maintenance");

    private final String code;
    private final String displayNameTr;
    private final String displayNameEn;

    TenantStatus(String code, String displayNameTr, String displayNameEn) {
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

    public String getDisplayNameTr() {
        return displayNameTr;
    }

    public String getDisplayNameEn() {
        return displayNameEn;
    }

    public static TenantStatus fromCode(String code) {
        for (TenantStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported tenant status code: " + code);
    }
}