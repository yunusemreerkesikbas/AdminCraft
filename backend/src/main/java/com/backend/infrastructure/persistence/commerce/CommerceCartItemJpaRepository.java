package com.backend.infrastructure.persistence.commerce;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.commerce.CommerceCartItem;

interface CommerceCartItemJpaRepository extends JpaRepository<CommerceCartItem, Long> {

    Optional<CommerceCartItem> findByCartIdAndUid(Long cartId, String uid);
}
