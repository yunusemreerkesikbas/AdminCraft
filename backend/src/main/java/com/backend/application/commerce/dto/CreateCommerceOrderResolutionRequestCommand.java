package com.backend.application.commerce.dto;

import com.backend.domain.commerce.CommerceOrderResolutionRequestType;

public record CreateCommerceOrderResolutionRequestCommand(
		CommerceOrderResolutionRequestType type,
		String reason,
		String description) {
}
