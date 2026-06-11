package com.backend.domain.commerce.repository;

import java.util.Optional;

import com.backend.domain.commerce.CommerceCartItem;

public interface CommerceCartItemRepository {

    Optional<CommerceCartItem> findByCartIdAndUid(Long cartId, String uid);
}
