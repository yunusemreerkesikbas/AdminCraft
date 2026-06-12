package com.backend.domain.commerce.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import com.backend.domain.commerce.CommerceCart;
import com.backend.domain.commerce.CommerceCartStatus;

public interface CommerceCartRepository {

    CommerceCart save(CommerceCart cart);

    Optional<CommerceCart> findByTokenHashAndStatus(String tokenHash, CommerceCartStatus status);

	Optional<CommerceCart> findByTokenHashAndStatusForUpdate(String tokenHash, CommerceCartStatus status);

	Optional<CommerceCart> findFirstByCustomerIdAndStatusAndExpiresAtAfterOrderByIdAsc(
			Long customerId,
			CommerceCartStatus status,
			LocalDateTime now);
}
