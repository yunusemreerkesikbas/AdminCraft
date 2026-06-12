package com.backend.domain.commerce.repository;

import java.util.Optional;

import com.backend.domain.commerce.CommerceCustomerRefreshToken;

public interface CommerceCustomerRefreshTokenRepository {

	CommerceCustomerRefreshToken save(CommerceCustomerRefreshToken refreshToken);

	Optional<CommerceCustomerRefreshToken> findByTokenHash(String tokenHash);

	int revokeByTokenHash(String tokenHash);
}
