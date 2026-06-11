package com.backend.domain.commerce.repository;

import java.util.Optional;

import com.backend.domain.commerce.CommerceCart;
import com.backend.domain.commerce.CommerceCartStatus;

public interface CommerceCartRepository {

    CommerceCart save(CommerceCart cart);

    Optional<CommerceCart> findByTokenHashAndStatus(String tokenHash, CommerceCartStatus status);
}
