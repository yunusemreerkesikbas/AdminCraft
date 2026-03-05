package com.backend.application.dto.mail;

public record MailTemplateDto(
    Long id,
    String templateKey,
    String language,
    String subject,
    String content,
    Boolean active
) {
}
