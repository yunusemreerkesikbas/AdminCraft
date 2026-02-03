package com.backend.domain.enums;

public enum TokenType {
    EMAIL_VERIFY("email_verify", "Email Doğrulama", "Email Verification", 3600),      // 1 hour
    PASSWORD_RESET("password_reset", "Şifre Sıfırlama", "Password Reset", 3600),      // 1 hour
    LOGIN_OTP("login_otp", "Giriş OTP", "Login OTP", 300),                            // 5 minutes
    OPERATION_OTP("operation_otp", "İşlem OTP", "Operation OTP", 300);                // 5 minutes

    private final String code;
    private final String displayNameTr;
    private final String displayNameEn;
    private final int defaultExpirySeconds;

    TokenType(String code, String displayNameTr, String displayNameEn, int defaultExpirySeconds) {
        this.code = code;
        this.displayNameTr = displayNameTr;
        this.displayNameEn = displayNameEn;
        this.defaultExpirySeconds = defaultExpirySeconds;
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

    public int getDefaultExpirySeconds() {
        return defaultExpirySeconds;
    }

    public boolean isOtp() {
        return this == LOGIN_OTP || this == OPERATION_OTP;
    }
}
