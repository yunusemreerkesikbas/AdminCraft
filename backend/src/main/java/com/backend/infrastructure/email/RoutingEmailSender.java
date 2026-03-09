package com.backend.infrastructure.email;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.backend.application.dto.email.EmailResult;
import com.backend.application.service.config.GlobalRuntimeConfigService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class RoutingEmailSender implements EmailSender {

    private final ConsoleEmailSender consoleEmailSender;
    private final SmtpEmailSender smtpEmailSender;
    private final GlobalRuntimeConfigService globalRuntimeConfigService;

    @Override
    public EmailResult send(String to, String subject, String htmlContent) {
        return selectSender().send(to, subject, htmlContent);
    }

    @Override
    public boolean isAvailable() {
        return selectSender().isAvailable();
    }

    private EmailSender selectSender() {
        String provider = globalRuntimeConfigService.getEmailProvider();
        if ("smtp".equalsIgnoreCase(provider)) {
            if (!smtpEmailSender.isAvailable()) {
                log.warn("SMTP provider selected but is not available, falling back to console sender");
                return consoleEmailSender;
            }
            return smtpEmailSender;
        }
        if (!"console".equalsIgnoreCase(provider)) {
            log.warn("Unknown email provider [{}], falling back to console sender", provider);
        }
        return consoleEmailSender;
    }
}

