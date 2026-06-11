package com.backend.infrastructure.persistence.commerce;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.backend.domain.commerce.CommerceCartItem;
import com.backend.domain.commerce.repository.CommerceCartItemRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CommerceCartItemRepositoryImpl implements CommerceCartItemRepository {

    private final CommerceCartItemJpaRepository jpaRepository;

    @Override
    public Optional<CommerceCartItem> findByCartIdAndUid(Long cartId, String uid) {
        return jpaRepository.findByCartIdAndUid(cartId, uid);
    }
}
