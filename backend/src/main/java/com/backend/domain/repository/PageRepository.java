package com.backend.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.Page;
import com.backend.domain.enums.PageStatus;
import com.backend.domain.enums.PageType;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {

    Optional<Page> findByUuid(String uuid);

    Optional<Page> findByUid(String uid);

    List<Page> findByStatus(PageStatus status);

    List<Page> findByTemplateId(Long templateId);

    boolean existsByUid(String uid);

    List<Page> findByUidIn(List<String> uids);

    long countByStatus(PageStatus status);

    int countByCreatedAtAfter(LocalDateTime date);

    Optional<Page> findByIsHomeTrue();

    Optional<Page> findFirstByPageType(PageType pageType);

    Optional<Page> findByIsHomeTrueAndStatus(PageStatus status);

    Optional<Page> findFirstByPageTypeAndStatusOrderByIdAsc(PageType pageType, PageStatus status);

    Optional<Page> findByIdAndStatus(Long id, PageStatus status);

    long countByIsHomeTrueAndStatus(PageStatus status);

    long countByPageTypeAndStatus(PageType pageType, PageStatus status);

    /** Preview-aware home page lookup. */
    Optional<Page> findByIsHomeTrueAndStatusIn(java.util.Collection<PageStatus> statuses);

    /** Preview-aware unique-template page lookup (e.g. PRODUCT, SEARCH). */
    Optional<Page> findFirstByPageTypeAndStatusInOrderByIdAsc(PageType pageType, java.util.Collection<PageStatus> statuses);

    /** Preview-aware page lookup by id when status must be one of a permitted set. */
    Optional<Page> findByIdAndStatusIn(Long id, java.util.Collection<PageStatus> statuses);
}
