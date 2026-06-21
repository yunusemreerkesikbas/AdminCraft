package com.backend.application.commerce.dto;

public record CommerceNotificationTemplateCommand(
		String subject,
		String content,
		Boolean active) {
}
