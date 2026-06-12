package com.backend.application.commerce;

public record CommerceCustomerPrincipal(
		Long customerId,
		String customerUid,
		String email,
		Long tenantId) {
}
