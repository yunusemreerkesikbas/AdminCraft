package com.backend.application.service;

import com.backend.application.dto.email.EmailContext;
import com.backend.application.dto.email.EmailResult;
import com.backend.domain.enums.Language;

public interface EmailService {

    EmailResult sendEmail(EmailContext context);

    EmailResult sendOtpEmail(String toEmail, String otpCode, Language language);

    EmailResult sendPasswordResetEmail(String toEmail, String resetToken, String subdomain, Language language);

    EmailResult sendEmailVerificationEmail(String toEmail, String verificationToken, String subdomain, Language language);
}
