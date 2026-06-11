package com.backend.infrastructure.persistence.commerce;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.commerce.CommerceCart;
import com.backend.domain.commerce.CommerceCartStatus;

interface CommerceCartJpaRepository extends JpaRepository<CommerceCart, Long> {

    @EntityGraph(attributePaths = { "items" })
    Optional<CommerceCart> findByTokenHashAndStatus(String tokenHash, CommerceCartStatus status);
}
