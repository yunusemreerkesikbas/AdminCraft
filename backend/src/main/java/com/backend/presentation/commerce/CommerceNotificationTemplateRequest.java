package com.backend.presentation.commerce;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommerceNotificationTemplateRequest(
		@NotBlank @Size(max = 255) String subject,
		@NotBlank @Size(max = 20000) String content,
		@NotNull Boolean active) {
}
