package com.backend.application.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.backend.application.dto.email.EmailContext;
import com.backend.application.dto.email.EmailResult;
import com.backend.application.service.EmailService;
import com.backend.domain.enums.EmailType;
import com.backend.domain.enums.Language;
import com.backend.domain.port.EmailTemplateRendererPort;
import com.backend.domain.port.MailConfigPort;
import com.backend.domain.port.MailSenderPort;

import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final MailSenderPort emailSender;
    private final EmailTemplateRendererPort templateRenderer;
    private final MailConfigPort mailConfig;
    @Value("${app.frontend.admin-url:http://localhost:4200}")
    private String adminPanelUrl;

    @Override
    public EmailResult sendEmail(EmailContext context) {
        if (!mailConfig.isEnabled()) {
            log.info("Email sending is disabled, skipping email");
            return EmailResult.success("disabled");
        }

        try {
            // Check if console mode + simplified logging
            boolean isConsoleModeSimplified =
                "console".equalsIgnoreCase(mailConfig.getProvider()) &&
                mailConfig.isLogSimplified();

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
            log.error("Failed to send email: email send failed");
            return EmailResult.failure("email send failed");
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
        Language normalized = (language == Language.TR) ? Language.TR : Language.EN;
        String subjectKey = "email.subject." + emailType.getCode();
        return templateRenderer.getSubject(subjectKey, normalized);
    }

    private String buildPasswordResetLink(String token, String subdomain) {
        return String.format("%s/reset-password?token=%s&subdomain=%s", buildBaseUrl(), token, subdomain);
    }

    private String buildEmailVerificationLink(String token, String subdomain) {
        return String.format("%s/set-password?token=%s&subdomain=%s", buildBaseUrl(), token, subdomain);
    }

    private String buildBaseUrl() {
        return adminPanelUrl;
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
        return (String) vars.get("otpCode");
    }

    private String formatPasswordResetContent(Map<String, Object> vars) {
        return (String) vars.get("resetLink");
    }

    private String formatEmailVerifyContent(Map<String, Object> vars) {
        return (String) vars.get("verificationLink");
    }
}
