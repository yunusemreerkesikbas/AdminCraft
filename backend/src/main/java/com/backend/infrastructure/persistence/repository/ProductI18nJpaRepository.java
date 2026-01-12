package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ProductI18n;
import com.backend.domain.enums.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface ProductI18nJpaRepository extends JpaRepository<ProductI18n, Long> {

    Optional<ProductI18n> findByProductIdAndLanguage(Long productId, Language language);

    List<ProductI18n> findByProductId(Long productId);

    boolean existsByProductIdAndLanguage(Long productId, Language language);

    @Modifying
    @Query("DELETE FROM ProductI18n pi WHERE pi.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}
