package com.backend.infrastructure.email;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.backend.application.dto.email.EmailResult;
import com.backend.domain.port.MailConfigPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsoleEmailSender implements EmailSender {

    private final MailConfigPort mailConfig;
    private final EmailProperties emailProperties;

    @Override
    public EmailResult send(String to, String subject, String htmlContent) {
        String messageId = UUID.randomUUID().toString();

        log.info("========================================");
        log.info("EMAIL (Console Mode)");
        log.info("========================================");
        log.info("To: {}", maskEmail(to));
        log.info("From: {} <{}>", mailConfig.getFromName(), mailConfig.getFromAddress());
        log.info("Subject: {}", subject);
        log.info("Message ID: {}", messageId);
        log.info("----------------------------------------");

        if (emailProperties.isLogContent()) {
            log.info("Content:\n{}", htmlContent);
        } else if (emailProperties.isLogSimplified()) {
            log.info("{}", htmlContent);  // Already formatted by EmailServiceImpl
        } else {
            log.info("Content: [HIDDEN - set app.email.log-content=true to show]");
        }

        log.info("========================================");

        return EmailResult.success(messageId);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (local.isEmpty()) {
            return domain;
        }
        return local.charAt(0) + "***" + domain;
    }
}
