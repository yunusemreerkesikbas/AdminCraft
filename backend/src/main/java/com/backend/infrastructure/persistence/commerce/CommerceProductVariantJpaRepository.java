package com.backend.infrastructure.persistence.commerce;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.entity.ProductVariant;

interface CommerceProductVariantJpaRepository extends JpaRepository<ProductVariant, Long> {

    @EntityGraph(attributePaths = { "product" })
    Optional<ProductVariant> findByUid(String uid);

    @EntityGraph(attributePaths = { "product" })
    List<ProductVariant> findByUidIn(Collection<String> uids);
}
