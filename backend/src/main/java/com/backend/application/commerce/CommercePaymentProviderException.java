package com.backend.application.commerce;

public class CommercePaymentProviderException extends RuntimeException {

	public CommercePaymentProviderException(String messageKey) {
		super(messageKey);
	}

	public CommercePaymentProviderException(String messageKey, Throwable cause) {
		super(messageKey, cause);
	}
}
