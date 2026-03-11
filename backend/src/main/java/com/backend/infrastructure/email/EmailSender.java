package com.backend.infrastructure.email;

import com.backend.application.dto.email.EmailResult;
import com.backend.domain.port.MailSenderPort;

public interface EmailSender extends MailSenderPort {

    EmailResult send(String to, String subject, String htmlContent);

    boolean isAvailable();
}
