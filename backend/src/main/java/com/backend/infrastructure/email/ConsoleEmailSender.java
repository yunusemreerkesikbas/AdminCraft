package com.backend.infrastructure.email;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.backend.application.dto.email.EmailResult;
import com.backend.domain.port.ConsoleMailSenderPort;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ConsoleEmailSender implements EmailSender, ConsoleMailSenderPort {

    @Override
    public EmailResult send(String to, String subject, String htmlContent) {
        String messageId = UUID.randomUUID().toString();
        log.info("[MAIL:CONSOLE] messageId={} channel=console status=accepted", messageId);
        return EmailResult.success(messageId);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
