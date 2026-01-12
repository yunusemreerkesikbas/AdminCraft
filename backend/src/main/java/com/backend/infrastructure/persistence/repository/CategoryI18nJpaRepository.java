package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.CategoryI18n;
import com.backend.domain.enums.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface CategoryI18nJpaRepository extends JpaRepository<CategoryI18n, Long> {

    Optional<CategoryI18n> findByCategoryIdAndLanguage(Long categoryId, Language language);

    List<CategoryI18n> findByCategoryId(Long categoryId);

    boolean existsByCategoryIdAndLanguage(Long categoryId, Language language);

    @Modifying
    @Query("DELETE FROM CategoryI18n ci WHERE ci.category.id = :categoryId")
    void deleteByCategoryId(@Param("categoryId") Long categoryId);
}
