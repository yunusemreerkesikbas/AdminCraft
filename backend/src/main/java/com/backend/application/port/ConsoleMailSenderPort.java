package com.backend.application.port;

import com.backend.application.dto.email.EmailResult;

public interface ConsoleMailSenderPort {

    EmailResult send(String to, String subject, String htmlContent);
}
