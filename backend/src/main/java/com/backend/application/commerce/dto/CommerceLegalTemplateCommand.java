package com.backend.application.commerce.dto;

import com.backend.domain.commerce.CommerceLegalTemplateType;

public record CommerceLegalTemplateCommand(
		CommerceLegalTemplateType type,
		String language,
		String title,
		String contentText) {
}
