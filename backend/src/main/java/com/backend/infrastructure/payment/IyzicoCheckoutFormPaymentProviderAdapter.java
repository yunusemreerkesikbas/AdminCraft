package com.backend.infrastructure.payment;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.backend.application.commerce.CommercePaymentProviderException;
import com.backend.application.commerce.CommercePaymentProviderPort;
import com.backend.application.commerce.CommercePaymentProviderPort.Credentials;
import com.iyzipay.Options;
import com.iyzipay.model.BasketItemType;
import com.iyzipay.model.CheckoutForm;
import com.iyzipay.model.CheckoutFormInitialize;
import com.iyzipay.model.Currency;
import com.iyzipay.model.Locale;
import com.iyzipay.model.PaymentGroup;
import com.iyzipay.model.Refund;
import com.iyzipay.model.Status;
import com.iyzipay.request.CreateCheckoutFormInitializeRequest;
import com.iyzipay.request.CreateRefundV2Request;
import com.iyzipay.request.RetrieveCheckoutFormRequest;

@Component
class IyzicoCheckoutFormPaymentProviderAdapter implements CommercePaymentProviderPort {

	private static final String PROVIDER_CODE = "iyzico";
	private static final String PAYMENT_STATUS_SUCCESS = "SUCCESS";

	@Override
	public String providerCode() {
		return PROVIDER_CODE;
	}

	@Override
	public CheckoutFormInitializeResult initializeCheckoutForm(CheckoutFormInitializeCommand command) {
		try {
			CheckoutFormInitialize initialize = CheckoutFormInitialize.create(toRequest(command), options(command.credentials()));
			if (!Status.SUCCESS.getValue().equals(initialize.getStatus())
					|| !initialize.verifySignature(command.credentials().secretKey())) {
				throw new CommercePaymentProviderException("commerce.payment.provider.initialize.failed");
			}
			return toResult(initialize);
		} catch (CommercePaymentProviderException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			throw new CommercePaymentProviderException("commerce.payment.provider.initialize.failed", ex);
		}
	}

	@Override
	public CheckoutFormResult retrieveCheckoutForm(CheckoutFormRetrieveCommand command) {
		try {
			RetrieveCheckoutFormRequest request = new RetrieveCheckoutFormRequest();
			request.setLocale(Locale.TR.getValue());
			request.setConversationId(command.conversationId());
			request.setToken(command.token());
			CheckoutForm checkoutForm = CheckoutForm.retrieve(request, options(command.credentials()));
			if (!checkoutForm.verifySignature(command.credentials().secretKey())) {
				return new CheckoutFormResult(
						false,
						null,
						"INVALID_SIGNATURE",
						"commerce.payment.provider.signature.invalid");
			}
			boolean success = Status.SUCCESS.getValue().equals(checkoutForm.getStatus())
					&& PAYMENT_STATUS_SUCCESS.equalsIgnoreCase(checkoutForm.getPaymentStatus());
			return new CheckoutFormResult(
					success,
					checkoutForm.getPaymentId(),
					checkoutForm.getErrorCode(),
					success ? null : "commerce.payment.provider.failed");
		} catch (RuntimeException ex) {
			throw new CommercePaymentProviderException("commerce.payment.provider.retrieve.failed", ex);
		}
	}

	@Override
	public RefundPaymentResult refundPayment(RefundPaymentCommand command) {
		try {
			CreateRefundV2Request request = new CreateRefundV2Request();
			request.setLocale(Locale.TR.getValue());
			request.setConversationId(command.conversationId());
			request.setPaymentId(command.paymentId());
			request.setPrice(command.price());
			request.setIp(command.clientIp());
			Refund refund = Refund.createV2(request, options(command.credentials()));
			if (!refund.verifySignature(command.credentials().secretKey())) {
				return new RefundPaymentResult(
						false,
						null,
						"INVALID_SIGNATURE",
						"commerce.payment.provider.signature.invalid");
			}
			return toRefundResult(refund);
		} catch (RuntimeException ex) {
			throw new CommercePaymentProviderException("commerce.payment.refund.failed", ex);
		}
	}

	RefundPaymentResult toRefundResult(Refund refund) {
		boolean success = Status.SUCCESS.getValue().equals(refund.getStatus());
		return new RefundPaymentResult(
				success,
				success ? reference(refund) : null,
				refund.getErrorCode(),
				success ? null : "commerce.payment.refund.failed");
	}

	private CreateCheckoutFormInitializeRequest toRequest(CheckoutFormInitializeCommand command) {
		CreateCheckoutFormInitializeRequest request = new CreateCheckoutFormInitializeRequest();
		request.setLocale(Locale.TR.getValue());
		request.setConversationId(command.conversationId());
		request.setPrice(command.subtotal());
		request.setPaidPrice(command.total());
		request.setCurrency(currency(command.currencyIso()));
		request.setBasketId(command.checkoutUid());
		request.setPaymentGroup(PaymentGroup.PRODUCT.name());
		request.setCallbackUrl(command.callbackUrl());
		request.setDebitCardAllowed(Boolean.TRUE);
		request.setEnabledInstallments(List.of(1));
		request.setBuyer(toBuyer(command.buyer()));
		request.setShippingAddress(toAddress(command.shippingAddress()));
		request.setBillingAddress(toAddress(command.billingAddress()));
		request.setBasketItems(command.basketItems().stream()
				.map(this::toBasketItem)
				.toList());
		return request;
	}

	CheckoutFormInitializeResult toResult(CheckoutFormInitialize initialize) {
		if (!StringUtils.hasText(initialize.getToken()) || !StringUtils.hasText(initialize.getPaymentPageUrl())) {
			throw new CommercePaymentProviderException("commerce.payment.provider.initialize.failed");
		}
		return new CheckoutFormInitializeResult(
				initialize.getToken().trim(),
				initialize.getPaymentPageUrl().trim());
	}

	private com.iyzipay.model.Buyer toBuyer(CommercePaymentProviderPort.Buyer source) {
		com.iyzipay.model.Buyer buyer = new com.iyzipay.model.Buyer();
		buyer.setId(source.id());
		buyer.setName(source.firstName());
		buyer.setSurname(source.lastName());
		buyer.setGsmNumber(source.phone());
		buyer.setEmail(source.email());
		buyer.setIdentityNumber(source.identityNumber());
		buyer.setIp(source.ipAddress());
		buyer.setRegistrationAddress(source.registrationAddress());
		buyer.setCity(source.city());
		buyer.setCountry(source.country());
		buyer.setZipCode(source.postalCode());
		return buyer;
	}

	private com.iyzipay.model.Address toAddress(CommercePaymentProviderPort.Address source) {
		com.iyzipay.model.Address address = new com.iyzipay.model.Address();
		address.setContactName(source.contactName());
		address.setCity(source.city());
		address.setCountry(source.country());
		address.setAddress(source.address());
		address.setZipCode(source.postalCode());
		return address;
	}

	private com.iyzipay.model.BasketItem toBasketItem(CommercePaymentProviderPort.BasketItem source) {
		com.iyzipay.model.BasketItem item = new com.iyzipay.model.BasketItem();
		item.setId(source.id());
		item.setName(source.name());
		item.setCategory1(source.category());
		item.setItemType(BasketItemType.PHYSICAL.name());
		item.setPrice(source.price());
		return item;
	}

	private Options options(Credentials credentials) {
		Options options = new Options();
		options.setApiKey(credentials.apiKey());
		options.setSecretKey(credentials.secretKey());
		options.setBaseUrl(credentials.baseUrl());
		return options;
	}

	private String currency(String currencyIso) {
		return Currency.TRY.name().equalsIgnoreCase(currencyIso)
				? Currency.TRY.name()
				: currencyIso;
	}

	private String reference(Refund refund) {
		if (StringUtils.hasText(refund.getRefundHostReference())) {
			return refund.getRefundHostReference().trim();
		}
		if (StringUtils.hasText(refund.getPaymentId())) {
			return refund.getPaymentId().trim();
		}
		return null;
	}
}
