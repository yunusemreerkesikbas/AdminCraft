package com.backend.application.commerce;

import com.backend.application.commerce.CommercePaymentProviderPort.Credentials;

public interface CommercePaymentConfigResolver {

	Credentials credentialsForProvider(String providerCode);
}
