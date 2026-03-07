package com.backend.application.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.backend.application.dto.email.EmailContext;
import com.backend.application.dto.email.EmailResult;
import com.backend.application.service.EmailService;
import com.backend.domain.enums.EmailType;
import com.backend.domain.enums.Language;
import com.backend.domain.port.FrontendConfigPort;
import com.backend.domain.port.MailConfigPort;
import com.backend.infrastructure.email.EmailProperties;
import com.backend.infrastructure.email.EmailSender;
import com.backend.infrastructure.email.EmailTemplateRenderer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final EmailSender emailSender;
    private final EmailTemplateRenderer templateRenderer;
    private final EmailProperties emailProperties;
    private final MailConfigPort mailConfig;
    private final FrontendConfigPort frontendConfig;

    @Override
    public EmailResult sendEmail(EmailContext context) {
        if (!emailProperties.isEnabled()) {
            log.info("Email sending is disabled, skipping email to: {}", context.getTo());
            return EmailResult.success("disabled");
        }

        try {
            // Check if console mode + simplified logging
            boolean isConsoleModeSimplified =
                "console".equalsIgnoreCase(mailConfig.getProvider()) &&
                emailProperties.isLogSimplified();

            String htmlContent;
            if (isConsoleModeSimplified) {
                // Skip template rendering, format essential data only
                htmlContent = formatSimplifiedContent(context);
            } else {
                // Normal flow: render full HTML template
                htmlContent = templateRenderer.render(context);
            }

            String subject = getSubjectForEmailType(context.getEmailType(), context.getLanguage());
            return emailSender.send(context.getTo(), subject, htmlContent);

        } catch (Exception e) {
            log.error("Failed to send email to: {}", context.getTo(), e);
            return EmailResult.failure(e.getMessage());
        }
    }

    @Override
    public EmailResult sendOtpEmail(String toEmail, String otpCode, Language language) {
        EmailContext context = EmailContext.builder()
                .to(toEmail)
                .emailType(EmailType.LOGIN_OTP)
                .language(language)
                .variables(Map.of(
                        "otpCode", otpCode,
                        "expiryMinutes", 5
                ))
                .build();

        return sendEmail(context);
    }

    @Override
    public EmailResult sendPasswordResetEmail(String toEmail, String resetToken, String subdomain, Language language) {
        String resetLink = buildPasswordResetLink(resetToken, subdomain);

        EmailContext context = EmailContext.builder()
                .to(toEmail)
                .emailType(EmailType.PASSWORD_RESET)
                .language(language)
                .variables(Map.of(
                        "resetLink", resetLink,
                        "resetToken", resetToken,
                        "expiryHours", 1
                ))
                .build();

        return sendEmail(context);
    }

    @Override
    public EmailResult sendEmailVerificationEmail(String toEmail, String verificationToken, String subdomain, Language language) {
        String verificationLink = buildEmailVerificationLink(verificationToken, subdomain);

        EmailContext context = EmailContext.builder()
                .to(toEmail)
                .emailType(EmailType.EMAIL_VERIFY)
                .language(language)
                .variables(Map.of(
                        "verificationLink", verificationLink,
                        "verificationToken", verificationToken,
                        "expiryHours", 24
                ))
                .build();

        return sendEmail(context);
    }

    private String getSubjectForEmailType(EmailType emailType, Language language) {
        // Şablon seçimi ile uyumlu olması için konuyu da TR veya EN olarak normalize ediyoruz.
        Language normalized = (language == Language.TR) ? Language.TR : Language.EN;
        String subjectKey = "email.subject." + emailType.getCode();
        return templateRenderer.getSubject(subjectKey, normalized);
    }

    private String buildPasswordResetLink(String token, String subdomain) {
        String baseUrl = buildBaseUrl(subdomain);
        return String.format("%s/reset-password?token=%s&subdomain=%s", baseUrl, token, subdomain);
    }

    private String buildEmailVerificationLink(String token, String subdomain) {
        String baseUrl = buildBaseUrl(subdomain);
        return String.format("%s/set-password?token=%s&subdomain=%s", baseUrl, token, subdomain);
    }

    private String buildBaseUrl(String subdomain) {
        String urlTemplate = frontendConfig.getBaseUrl();
        if (urlTemplate.contains("%s")) {
            return String.format(urlTemplate, subdomain);
        }
        return urlTemplate;
    }

    private String formatSimplifiedContent(EmailContext context) {
        Map<String, Object> vars = context.getVariables();

        return switch (context.getEmailType()) {
            case LOGIN_OTP, OPERATION_OTP -> formatOtpContent(vars);
            case PASSWORD_RESET -> formatPasswordResetContent(vars);
            case EMAIL_VERIFY -> formatEmailVerifyContent(vars);
        };
    }

    private String formatOtpContent(Map<String, Object> vars) {
        String code = (String) vars.get("otpCode");
        Integer expiry = (Integer) vars.get("expiryMinutes");

        return String.format("""

            ┌─────────────────────────────────────┐
            │  VERIFICATION CODE                  │
            │                                     │
            │          %s                   │
            │                                     │
            │  Expires in: %d minutes             │
            └─────────────────────────────────────┘

            💡 Copy code: %s
            """, code, expiry, code);
    }

    private String formatPasswordResetContent(Map<String, Object> vars) {
        String link = (String) vars.get("resetLink");
        String token = (String) vars.get("resetToken");
        Integer expiry = (Integer) vars.get("expiryHours");

        return String.format("""

            🔑 PASSWORD RESET

            Reset Link:
            %s

            Token: %s
            Expires: %d hour(s)

            💡 Copy and paste link in browser
            """, link, token, expiry);
    }

    private String formatEmailVerifyContent(Map<String, Object> vars) {
        String link = (String) vars.get("verificationLink");
        String token = (String) vars.get("verificationToken");
        Integer expiry = (Integer) vars.get("expiryHours");

        return String.format("""

            ✉️ EMAIL VERIFICATION

            Verification Link:
            %s

            Token: %s
            Expires: %d hour(s)

            💡 Copy and paste link in browser
            """, link, token, expiry);
    }
}
