package com.backend.infrastructure.persistence.commerce;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.commerce.CommerceCustomerRefreshToken;
import com.backend.domain.commerce.repository.CommerceCustomerRefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class CommerceCustomerRefreshTokenRepositoryImpl implements CommerceCustomerRefreshTokenRepository {

	private final CommerceCustomerRefreshTokenJpaRepository jpaRepository;

	@Override
	@Transactional
	public CommerceCustomerRefreshToken save(CommerceCustomerRefreshToken refreshToken) {
		return jpaRepository.save(refreshToken);
	}

	@Override
	public Optional<CommerceCustomerRefreshToken> findByTokenHash(String tokenHash) {
		return jpaRepository.findByTokenHash(tokenHash);
	}

	@Override
	@Transactional
	public int revokeByTokenHash(String tokenHash) {
		return jpaRepository.revokeByTokenHash(tokenHash, LocalDateTime.now());
	}
}
