package com.backend.domain.port;

import com.backend.application.dto.email.EmailResult;

public interface TenantMailSenderPort {

    EmailResult send(String serverToken, String fromEmail, String fromName, String to, String subject, String content);
}
