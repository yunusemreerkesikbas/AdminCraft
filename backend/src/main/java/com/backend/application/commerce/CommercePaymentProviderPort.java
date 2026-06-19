package com.backend.application.commerce;

import java.math.BigDecimal;
import java.util.List;

public interface CommercePaymentProviderPort {

	String providerCode();

	CheckoutFormInitializeResult initializeCheckoutForm(CheckoutFormInitializeCommand command);

	CheckoutFormResult retrieveCheckoutForm(CheckoutFormRetrieveCommand command);

	RefundPaymentResult refundPayment(RefundPaymentCommand command);

	record Credentials(
			String apiKey,
			String secretKey,
			String baseUrl) {
	}

	record CheckoutFormInitializeCommand(
			Credentials credentials,
			String conversationId,
			String attemptUid,
			String checkoutUid,
			BigDecimal subtotal,
			BigDecimal total,
			String currencyIso,
			String callbackUrl,
			Buyer buyer,
			Address shippingAddress,
			Address billingAddress,
			List<BasketItem> basketItems) {
	}

	record CheckoutFormRetrieveCommand(
			Credentials credentials,
			String conversationId,
			String token) {
	}

	record Buyer(
			String id,
			String firstName,
			String lastName,
			String phone,
			String email,
			String identityNumber,
			String ipAddress,
			String registrationAddress,
			String city,
			String country,
			String postalCode) {
	}

	record Address(
			String contactName,
			String city,
			String country,
			String address,
			String postalCode) {
	}

	record BasketItem(
			String id,
			String name,
			String category,
			BigDecimal price) {
	}

	record CheckoutFormInitializeResult(
			String token,
			String paymentPageUrl) {
	}

	record CheckoutFormResult(
			boolean successful,
			String providerTransactionId,
			String failureCode,
			String failureMessageKey) {
	}

	record RefundPaymentCommand(
			Credentials credentials,
			String conversationId,
			String paymentId,
			BigDecimal price,
			String currencyIso,
			String clientIp,
			String reason,
			String description) {
	}

	record RefundPaymentResult(
			boolean successful,
			String refundReference,
			String failureCode,
			String failureMessageKey) {
	}
}
