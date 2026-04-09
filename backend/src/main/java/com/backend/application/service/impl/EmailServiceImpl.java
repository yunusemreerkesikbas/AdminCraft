package com.backend.application.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.backend.application.dto.email.EmailContext;
import com.backend.application.dto.email.EmailResult;
import com.backend.application.service.EmailService;
import com.backend.application.service.TenantMailMarketingService;
import com.backend.application.service.TenantModuleAccessService;
import com.backend.application.service.mail.TemplateVariableRenderer;
import com.backend.domain.entity.MailTemplate;
import com.backend.domain.enums.EmailType;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.ModuleCode;
import com.backend.domain.port.EmailTemplateRendererPort;
import com.backend.domain.port.MailSenderPort;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.MailTemplateRepository;
import com.backend.shared.common.LogSanitizer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final String TENANT_USER_WELCOME = "TENANT_USER_WELCOME";

    private final MailSenderPort emailSender;
    private final EmailTemplateRendererPort templateRenderer;
    private final TenantContextPort tenantContext;
    private final TenantModuleAccessService tenantModuleAccessService;
    private final TenantMailMarketingService tenantMailMarketingService;
    private final MailTemplateRepository mailTemplateRepository;
    private final TemplateVariableRenderer templateVariableRenderer;
    @Value("${app.frontend.admin-url:http://localhost:4200}")
    private String adminPanelUrl;

    @Override
    public EmailResult sendEmail(EmailContext context) {
        try {
            String htmlContent = templateRenderer.render(context);
            String subject = getSubjectForEmailType(context.getEmailType(), context.getLanguage());
            return emailSender.send(context.getTo(), subject, htmlContent);
        } catch (Exception e) {
            log.error(
                    "Failed to send email type={} recipient={}",
                    context.getEmailType(),
                    LogSanitizer.maskEmail(context.getTo()),
                    e);
            return EmailResult.failure("email send failed");
        }
    }

    @Override
    public EmailResult sendOtpEmail(String toEmail, String otpCode, Language language) {
        log.info("[MAIL] otp dispatch requested | recipient={}", LogSanitizer.maskEmail(toEmail));
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
        log.info("[MAIL] password-reset dispatch requested | recipient={}", LogSanitizer.maskEmail(toEmail));

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
        log.info("[MAIL] email-verify dispatch requested | recipient={}", LogSanitizer.maskEmail(toEmail));
        EmailContext defaultContext = EmailContext.builder()
                .to(toEmail)
                .emailType(EmailType.EMAIL_VERIFY)
                .language(language)
                .variables(Map.of(
                        "verificationLink", verificationLink,
                        "verificationToken", verificationToken,
                        "expiryHours", 24
                ))
                .build();

        if (tenantContext.isSet() && tenantModuleAccessService.isEnabledForCurrentTenant(ModuleCode.MAIL_MARKETING)) {
            String langCode = language == Language.TR ? "TR" : "EN";
            MailTemplate customTemplate = mailTemplateRepository
                    .findByTemplateKeyIgnoreCaseAndLanguageIgnoreCase(TENANT_USER_WELCOME, langCode)
                    .filter(t -> Boolean.TRUE.equals(t.getIsActive()))
                    .orElse(null);

            if (customTemplate != null) {
                String name = toEmail.contains("@") ? toEmail.substring(0, toEmail.indexOf('@')) : toEmail;
                Map<String, String> vars = Map.of(
                        "name", name,
                        "verificationLink", verificationLink,
                        "expiryHours", "24"
                );
                String subject = templateVariableRenderer.render(customTemplate.getSubject(), vars);
                String content = templateVariableRenderer.render(customTemplate.getContent(), vars);
                return tenantMailMarketingService.sendTenantRawContent(toEmail, subject, content);
            }
            return sendTenantEmail(defaultContext);
        }

        return sendEmail(defaultContext);
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

    private EmailResult sendTenantEmail(EmailContext context) {
        try {
            String htmlContent = templateRenderer.render(context);
            String subject = getSubjectForEmailType(context.getEmailType(), context.getLanguage());
            return tenantMailMarketingService.sendTenantRawContent(context.getTo(), subject, htmlContent);
        } catch (Exception e) {
            log.error(
                    "Failed to send tenant email type={} recipient={}",
                    context.getEmailType(),
                    LogSanitizer.maskEmail(context.getTo()),
                    e);
            return EmailResult.failure("email send failed");
        }
    }
}
