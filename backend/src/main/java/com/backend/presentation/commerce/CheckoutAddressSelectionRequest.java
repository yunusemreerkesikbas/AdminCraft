package com.backend.presentation.commerce;

import com.backend.shared.validation.Uid;

public record CheckoutAddressSelectionRequest(
		@Uid String deliveryAddressUid,
		@Uid String billingAddressUid,
		Boolean billingSameAsDelivery) {
}
