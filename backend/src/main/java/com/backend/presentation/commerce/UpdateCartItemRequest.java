package com.backend.presentation.commerce;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemRequest(
	@NotNull(message = "{commerce.cart.quantity.required}") @Min(value = 1, message = "{commerce.cart.quantity.invalid}") @Max(value = 99, message = "{commerce.cart.quantity.invalid}") Integer quantity) {
}
