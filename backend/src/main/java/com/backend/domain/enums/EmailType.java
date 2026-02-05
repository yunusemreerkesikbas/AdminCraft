package com.backend.domain.enums;

public enum EmailType {
    EMAIL_VERIFY("email_verify", "Email Doğrulama", "Email Verification"),
    PASSWORD_RESET("password_reset", "Şifre Sıfırlama", "Password Reset"),
    LOGIN_OTP("login_otp", "Giriş OTP", "Login OTP"),
    OPERATION_OTP("operation_otp", "İşlem OTP", "Operation OTP");

    private final String code;
    private final String displayNameTr;
    private final String displayNameEn;

    EmailType(String code, String displayNameTr, String displayNameEn) {
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
}
