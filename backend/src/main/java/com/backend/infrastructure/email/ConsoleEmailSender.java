package com.backend.infrastructure.email;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.backend.application.dto.email.EmailResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ConsoleEmailSender implements EmailSender {

    @Override
    public EmailResult send(String to, String subject, String htmlContent) {
        String messageId = UUID.randomUUID().toString();
        log.info("[MAIL:CONSOLE] to={} | subject=\"{}\"", maskEmail(to), subject);
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
