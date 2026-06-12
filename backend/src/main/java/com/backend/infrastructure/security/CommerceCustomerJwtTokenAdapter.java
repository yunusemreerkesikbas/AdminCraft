package com.backend.infrastructure.security;

import org.springframework.stereotype.Component;

import com.backend.application.commerce.CommerceCustomerTokenPort;
import com.backend.domain.commerce.CommerceCustomer;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommerceCustomerJwtTokenAdapter implements CommerceCustomerTokenPort {

	private static final String CUSTOMER_ROLE = "COMMERCE_CUSTOMER";
	private static final String ACCESS_TYPE = "commerce_customer_access";
	private static final String REFRESH_TYPE = "commerce_customer_refresh";

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public String createAccessToken(CommerceCustomer customer, Long tenantId) {
		return jwtTokenProvider.createToken(
				customer.getEmail(),
				CUSTOMER_ROLE,
				customer.getId(),
				tenantId,
				ACCESS_TYPE,
				jwtTokenProvider.getAccessTokenExpiration(),
				false);
	}

	@Override
	public String createRefreshToken(CommerceCustomer customer, Long tenantId, boolean rememberMe) {
		return jwtTokenProvider.createToken(
				customer.getEmail(),
				CUSTOMER_ROLE,
				customer.getId(),
				tenantId,
				REFRESH_TYPE,
				jwtTokenProvider.getRefreshTokenExpiration(rememberMe),
				rememberMe);
	}

	@Override
	public boolean validateAccessToken(String token) {
		return jwtTokenProvider.validateToken(token) && ACCESS_TYPE.equals(jwtTokenProvider.getTokenType(token));
	}

	@Override
	public boolean validateRefreshToken(String token) {
		return jwtTokenProvider.validateToken(token) && REFRESH_TYPE.equals(jwtTokenProvider.getTokenType(token));
	}

	@Override
	public boolean isRememberMeToken(String token) {
		return jwtTokenProvider.isRememberMeToken(token);
	}

	@Override
	public Long getCustomerId(String token) {
		return jwtTokenProvider.getUserIdFromToken(token);
	}

	@Override
	public Long getTenantId(String token) {
		return jwtTokenProvider.getTenantIdFromToken(token);
	}

	@Override
	public String getEmail(String token) {
		return jwtTokenProvider.getEmailFromToken(token);
	}

	@Override
	public long getAccessTokenExpiration() {
		return jwtTokenProvider.getAccessTokenExpiration();
	}

	@Override
	public long getRefreshTokenExpiration(boolean rememberMe) {
		return jwtTokenProvider.getRefreshTokenExpiration(rememberMe);
	}
}
