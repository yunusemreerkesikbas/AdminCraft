package com.backend.application.commerce.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CheckoutResponse(
		String checkoutUid,
		String status,
		LocalDateTime expiresAt,
		CheckoutAddressSnapshotResponse deliveryAddress,
		CheckoutAddressSnapshotResponse billingAddress,
		List<CheckoutItemResponse> items,
		CheckoutTotalsResponse totals,
		CheckoutShippingResponse shipping,
		CheckoutValidationResponse validation,
		CheckoutLegalResponse legal) {
}
