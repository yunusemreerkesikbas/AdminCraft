package com.backend.application.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.backend.application.dto.email.EmailContext;
import com.backend.application.dto.email.EmailResult;
import com.backend.application.service.EmailService;
import com.backend.domain.enums.EmailType;
import com.backend.domain.enums.Language;
import com.backend.infrastructure.email.EmailProperties;
import com.backend.infrastructure.email.EmailSender;
import com.backend.infrastructure.email.EmailTemplateRenderer;
import com.backend.infrastructure.email.FrontendProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final EmailSender emailSender;
    private final EmailTemplateRenderer templateRenderer;
    private final EmailProperties emailProperties;
    private final FrontendProperties frontendProperties;

    @Override
    public EmailResult sendEmail(EmailContext context) {
        if (!emailProperties.isEnabled()) {
            log.info("Email sending is disabled, skipping email to: {}", context.getTo());
            return EmailResult.success("disabled");
        }

        try {
            String htmlContent = templateRenderer.render(context);
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
        String subjectKey = "email.subject." + emailType.getCode();
        return templateRenderer.getSubject(subjectKey, language);
    }

    private String buildPasswordResetLink(String token, String subdomain) {
        String baseUrl = buildBaseUrl(subdomain);
        return String.format("%s/auth/reset-password?token=%s&subdomain=%s", baseUrl, token, subdomain);
    }

    private String buildEmailVerificationLink(String token, String subdomain) {
        String baseUrl = buildBaseUrl(subdomain);
        return String.format("%s/auth/set-password?token=%s&subdomain=%s", baseUrl, token, subdomain);
    }

    private String buildBaseUrl(String subdomain) {
        String urlTemplate = frontendProperties.getBaseUrl();
        if (urlTemplate.contains("%s")) {
            return String.format(urlTemplate, subdomain);
        }
        return urlTemplate;
    }
}
