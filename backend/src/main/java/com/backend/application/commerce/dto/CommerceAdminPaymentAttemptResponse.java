package com.backend.application.commerce.dto;

import java.time.LocalDateTime;

import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.CommercePaymentAttempt;

public record CommerceAdminPaymentAttemptResponse(
		Long id,
		String attemptUid,
		String checkoutUid,
		String customerUid,
		String customerName,
		String customerEmail,
		String status,
		String provider,
		String currencyIso,
		PaymentAttemptTotalsResponse totals,
		LocalDateTime createdAt,
		LocalDateTime expiresAt,
		String providerReference,
		String providerTransactionId,
		String failureCode,
		String failureMessageKey) {

	public static CommerceAdminPaymentAttemptResponse from(CommercePaymentAttempt attempt) {
		CommerceCustomer customer = attempt.getCustomer();
		return new CommerceAdminPaymentAttemptResponse(
				attempt.getId(),
				attempt.getUid(),
				attempt.getCheckout().getUid(),
				customer.getUid(),
				CommerceCustomerNameFormatter.format(customer.getFirstName(), customer.getLastName()),
				customer.getEmail(),
				attempt.getStatus().name(),
				attempt.getProvider(),
				attempt.getCurrencyIso(),
				new PaymentAttemptTotalsResponse(
						attempt.getCurrencyIso(),
						attempt.getSubtotal(),
						attempt.getVatTotal(),
						attempt.getShippingTotal(),
						attempt.getTotal()),
				attempt.getCreatedAt(),
				attempt.getExpiresAt(),
				attempt.getProviderReference(),
				attempt.getProviderTransactionId(),
				attempt.getFailureCode(),
				attempt.getFailureMessageKey());
	}
}
