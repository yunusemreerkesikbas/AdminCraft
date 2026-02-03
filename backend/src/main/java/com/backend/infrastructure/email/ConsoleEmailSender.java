package com.backend.infrastructure.email;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.backend.application.dto.email.EmailResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.email.provider", havingValue = "console", matchIfMissing = true)
public class ConsoleEmailSender implements EmailSender {

    private final EmailProperties emailProperties;

    @Override
    public EmailResult send(String to, String subject, String htmlContent) {
        String messageId = UUID.randomUUID().toString();

        log.info("========================================");
        log.info("EMAIL (Console Mode)");
        log.info("========================================");
        log.info("To: {}", to);
        log.info("From: {} <{}>", emailProperties.getFromName(), emailProperties.getFromAddress());
        log.info("Subject: {}", subject);
        log.info("Message ID: {}", messageId);
        log.info("----------------------------------------");

        if (emailProperties.isLogContent()) {
            log.info("Content:\n{}", htmlContent);
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
}
