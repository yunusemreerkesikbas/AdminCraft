package com.backend.application.commerce;

import com.backend.application.commerce.dto.CommerceCustomerAuthResponse;
import com.backend.application.commerce.dto.LoginCommerceCustomerCommand;
import com.backend.application.commerce.dto.RegisterCommerceCustomerCommand;

public interface CommerceCustomerAuthService extends CommerceApplicationService {

	CommerceCustomerAuthResult register(RegisterCommerceCustomerCommand command);

	CommerceCustomerAuthResult login(LoginCommerceCustomerCommand command);

	CommerceCustomerAuthResult refresh(String refreshToken, String deviceFingerprint, String ipAddress, String userAgent);

	void logout(String refreshToken);

	record CommerceCustomerAuthResult(CommerceCustomerAuthResponse response, String refreshToken, boolean rememberMe) {
	}
}
