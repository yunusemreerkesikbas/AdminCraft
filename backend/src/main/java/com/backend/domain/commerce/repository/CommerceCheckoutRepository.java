package com.backend.domain.commerce.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import com.backend.domain.commerce.CommerceCheckout;
import com.backend.domain.commerce.CommerceCheckoutStatus;

public interface CommerceCheckoutRepository {

	CommerceCheckout save(CommerceCheckout checkout);

	Optional<CommerceCheckout> findByIdForUpdate(Long id);

	Optional<CommerceCheckout> findByCustomerIdAndUid(Long customerId, String uid);

	Optional<CommerceCheckout> findFirstByCustomerIdAndStatusInAndExpiresAtAfterOrderByIdDesc(
			Long customerId,
			Collection<CommerceCheckoutStatus> statuses,
			LocalDateTime now);

	int expireOpenCheckouts(Long customerId, Collection<CommerceCheckoutStatus> statuses, LocalDateTime now);
}
