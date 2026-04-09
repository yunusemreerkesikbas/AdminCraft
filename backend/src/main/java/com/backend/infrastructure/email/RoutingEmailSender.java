package com.backend.infrastructure.email;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.backend.application.dto.email.EmailResult;
import com.backend.application.service.config.GlobalRuntimeConfigService;
import com.backend.domain.port.TenantMailSenderPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class RoutingEmailSender implements EmailSender {

    private final ConsoleEmailSender consoleEmailSender;
    private final GlobalRuntimeConfigService globalRuntimeConfigService;
    private final TenantMailSenderPort postmarkSender;

    @Override
    public EmailResult send(String to, String subject, String htmlContent) {
        String provider = globalRuntimeConfigService.getEmailProvider();
        if ("postmark".equalsIgnoreCase(provider)) {
            String token = globalRuntimeConfigService.getPostmarkServerTokenDecrypted();
            if (token == null || token.isBlank()) {
                log.warn("Postmark provider selected but server token is not configured, falling back to console");
                return consoleEmailSender.send(to, subject, htmlContent);
            }
            return postmarkSender.send(
                token,
                globalRuntimeConfigService.getEmailFromAddress(),
                globalRuntimeConfigService.getEmailFromName(),
                to, subject, htmlContent);
        }
        return consoleEmailSender.send(to, subject, htmlContent);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
