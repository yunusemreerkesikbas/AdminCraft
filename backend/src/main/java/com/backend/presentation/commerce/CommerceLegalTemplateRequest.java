package com.backend.presentation.commerce;

import com.backend.domain.commerce.CommerceLegalTemplateType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommerceLegalTemplateRequest(
		@NotNull(message = "{commerce.legal.template.type.required}") CommerceLegalTemplateType type,
		@NotBlank(message = "{commerce.legal.template.language.required}") @Size(max = 10) String language,
		@NotBlank(message = "{commerce.legal.template.title.required}") @Size(max = 191) String title,
		@NotBlank(message = "{commerce.legal.template.content.required}") @Size(max = 20000) String contentText) {
}
