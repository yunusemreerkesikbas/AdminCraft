package com.backend.domain.port;

import com.backend.application.dto.email.EmailResult;

public interface MailSenderPort {

    EmailResult send(String to, String subject, String htmlContent);

    boolean isAvailable();
}
