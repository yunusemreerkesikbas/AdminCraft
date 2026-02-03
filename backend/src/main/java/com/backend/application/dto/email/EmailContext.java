package com.backend.application.dto.email;

import java.util.HashMap;
import java.util.Map;

import com.backend.domain.enums.EmailType;
import com.backend.domain.enums.Language;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailContext {

    private String to;
    private String subject;
    private EmailType emailType;
    private Language language;

    @Builder.Default
    private Map<String, Object> variables = new HashMap<>();

    public EmailContext addVariable(String key, Object value) {
        if (variables == null) {
            variables = new HashMap<>();
        }
        variables.put(key, value);
        return this;
    }

    public String getTemplateName() {
        String typeCode = emailType.getCode().replace("_", "-");
        String langCode = language.name().toLowerCase();
        return String.format("email/%s-%s", typeCode, langCode);
    }

    public static EmailContextBuilder forOtpLogin(String email, String otpCode, Language language) {
        return EmailContext.builder()
                .to(email)
                .emailType(EmailType.LOGIN_OTP)
                .language(language)
                .variables(Map.of(
                        "otpCode", otpCode,
                        "expiryMinutes", 5
                ));
    }

    public static EmailContextBuilder forPasswordReset(String email, String resetLink, Language language) {
        return EmailContext.builder()
                .to(email)
                .emailType(EmailType.PASSWORD_RESET)
                .language(language)
                .variables(Map.of(
                        "resetLink", resetLink,
                        "expiryHours", 1
                ));
    }

    public static EmailContextBuilder forEmailVerification(String email, String verificationLink, Language language) {
        return EmailContext.builder()
                .to(email)
                .emailType(EmailType.EMAIL_VERIFY)
                .language(language)
                .variables(Map.of(
                        "verificationLink", verificationLink,
                        "expiryHours", 1
                ));
    }
}
