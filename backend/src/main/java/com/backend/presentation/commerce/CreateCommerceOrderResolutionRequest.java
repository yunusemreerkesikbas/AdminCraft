package com.backend.presentation.commerce;

import com.backend.domain.commerce.CommerceOrderResolutionRequestType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCommerceOrderResolutionRequest(
		@NotNull(message = "commerce.order.request.type.required")
		CommerceOrderResolutionRequestType requestType,

		@NotBlank(message = "commerce.order.request.reason.required")
		@Size(max = 100, message = "commerce.order.request.reason.length")
		String reason,

		@NotBlank(message = "commerce.order.request.description.required")
		@Size(max = 1000, message = "commerce.order.request.description.length")
		String description) {
}
