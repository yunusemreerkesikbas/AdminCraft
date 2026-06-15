package com.backend.infrastructure.persistence.commerce;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.commerce.CommerceCheckout;
import com.backend.domain.commerce.CommerceCheckoutStatus;
import com.backend.domain.commerce.repository.CommerceCheckoutRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class CommerceCheckoutRepositoryImpl implements CommerceCheckoutRepository {

	private final CommerceCheckoutJpaRepository jpaRepository;

	@Override
	@Transactional
	public CommerceCheckout save(CommerceCheckout checkout) {
		return jpaRepository.save(checkout);
	}

	@Override
	public Optional<CommerceCheckout> findByIdForUpdate(Long id) {
		return jpaRepository.findByIdForUpdate(id);
	}

	@Override
	public Optional<CommerceCheckout> findByCustomerIdAndUid(Long customerId, String uid) {
		return jpaRepository.findByCustomerIdAndUid(customerId, uid);
	}

	@Override
	public Optional<CommerceCheckout> findFirstByCustomerIdAndStatusInAndExpiresAtAfterOrderByIdDesc(
			Long customerId,
			Collection<CommerceCheckoutStatus> statuses,
			LocalDateTime now) {
		return jpaRepository.findFirstByCustomerIdAndStatusInAndExpiresAtAfterOrderByIdDesc(customerId, statuses, now);
	}

	@Override
	@Transactional
	public int expireOpenCheckouts(Long customerId, Collection<CommerceCheckoutStatus> statuses, LocalDateTime now) {
		return jpaRepository.expireOpenCheckouts(customerId, statuses, now);
	}
}
