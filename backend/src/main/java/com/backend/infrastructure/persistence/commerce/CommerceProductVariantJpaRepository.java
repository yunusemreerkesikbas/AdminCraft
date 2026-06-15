package com.backend.infrastructure.persistence.commerce;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.entity.ProductVariant;

import jakarta.persistence.LockModeType;

interface CommerceProductVariantJpaRepository extends JpaRepository<ProductVariant, Long> {

    @EntityGraph(attributePaths = { "product" })
    Optional<ProductVariant> findByUid(String uid);

    @EntityGraph(attributePaths = { "product" })
    List<ProductVariant> findByUidIn(Collection<String> uids);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@EntityGraph(attributePaths = { "product" })
	@Query("SELECT variant FROM ProductVariant variant WHERE variant.uid IN :uids")
	List<ProductVariant> findByUidInForUpdate(@Param("uids") Collection<String> uids);
}
