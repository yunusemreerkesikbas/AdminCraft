package com.backend.infrastructure.email;

import com.backend.application.dto.email.EmailResult;

public interface EmailSender {

    EmailResult send(String to, String subject, String htmlContent);

    boolean isAvailable();
}
