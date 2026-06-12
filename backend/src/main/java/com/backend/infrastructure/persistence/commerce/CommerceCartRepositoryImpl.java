package com.backend.infrastructure.persistence.commerce;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.commerce.CommerceCart;
import com.backend.domain.commerce.CommerceCartStatus;
import com.backend.domain.commerce.repository.CommerceCartRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CommerceCartRepositoryImpl implements CommerceCartRepository {

    private final CommerceCartJpaRepository jpaRepository;

    @Override
    @Transactional
    public CommerceCart save(CommerceCart cart) {
        return jpaRepository.save(cart);
    }

    @Override
    public Optional<CommerceCart> findByTokenHashAndStatus(String tokenHash, CommerceCartStatus status) {
        return jpaRepository.findByTokenHashAndStatus(tokenHash, status);
    }

	@Override
	public Optional<CommerceCart> findByTokenHashAndStatusForUpdate(String tokenHash, CommerceCartStatus status) {
		return jpaRepository.findByTokenHashAndStatusForUpdate(tokenHash, status);
	}

	@Override
	public Optional<CommerceCart> findFirstByCustomerIdAndStatusAndExpiresAtAfterOrderByIdAsc(
			Long customerId,
			CommerceCartStatus status,
			LocalDateTime now) {
		return jpaRepository.findFirstByCustomerIdAndStatusAndExpiresAtAfterOrderByIdAsc(customerId, status, now);
	}
}
