package com.backend.application.commerce;

import com.backend.domain.commerce.CommerceCustomer;

public interface CommerceCustomerTokenPort {

	String createAccessToken(CommerceCustomer customer, Long tenantId);

	String createRefreshToken(CommerceCustomer customer, Long tenantId, boolean rememberMe);

	boolean validateAccessToken(String token);

	boolean validateRefreshToken(String token);

	boolean isRememberMeToken(String token);

	Long getCustomerId(String token);

	Long getTenantId(String token);

	String getEmail(String token);

	/**
	 * Returns the access token expiration duration in milliseconds.
	 */
	long getAccessTokenExpiration();

	/**
	 * Returns the refresh token expiration duration in milliseconds.
	 */
	long getRefreshTokenExpiration(boolean rememberMe);
}
