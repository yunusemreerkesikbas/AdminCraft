package com.backend.presentation.commerce;

import com.backend.domain.commerce.CommerceOrderStatus;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangeCommerceOrderStatusRequest(
		@NotNull CommerceOrderStatus status,
		@Size(max = 100) String carrierName,
		@Size(max = 100) String trackingNumber,
		@Size(max = 500) String trackingUrl,
		@Size(max = 1000) String internalNote) {

	@AssertTrue(message = "commerce.admin.order.status.transition.invalid")
	public boolean isStatusAllowed() {
		return status == CommerceOrderStatus.PREPARING
				|| status == CommerceOrderStatus.SHIPPED
				|| status == CommerceOrderStatus.DELIVERED;
	}
}
