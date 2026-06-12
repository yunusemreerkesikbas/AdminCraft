package com.backend.application.commerce.dto;

public record CheckoutAddressSelectionCommand(
		String deliveryAddressUid,
		String billingAddressUid,
		Boolean billingSameAsDelivery) {
}
