package com.backend.infrastructure.email;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import com.backend.application.dto.email.EmailResult;
import com.backend.domain.port.MailConfigPort;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final MailConfigPort mailConfig;

    @Override
    public EmailResult send(String to, String subject, String htmlContent) {
        try {
            JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
            if (mailSender == null) {
                log.error("SMTP provider selected but JavaMailSender bean is not available");
                return EmailResult.failure("SMTP sender is not available");
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailConfig.getFromAddress(), mailConfig.getFromName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            String messageId = message.getMessageID();
            log.info("Email sent successfully to: {}, messageId: {}", to, messageId);
            return EmailResult.success(messageId);

        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            return EmailResult.failure(e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        return mailSenderProvider.getIfAvailable() != null;
    }
}
