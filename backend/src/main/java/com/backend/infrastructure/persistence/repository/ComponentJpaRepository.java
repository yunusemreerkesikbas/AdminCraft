package com.backend.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.entity.Component;
import com.backend.domain.enums.ComponentStatus;

interface ComponentJpaRepository extends JpaRepository<Component, Long> {
    Optional<Component> findByUuid(String uuid);

    Optional<Component> findByUid(String uid);

    List<Component> findByComponentTypeId(Long componentTypeId);

    List<Component> findByStatus(ComponentStatus status);

    boolean existsByUid(String uid);

    @Query("""
                SELECT c, ct.uid
                FROM Component c
                LEFT JOIN ComponentType ct ON c.componentTypeId = ct.id
                ORDER BY c.updatedAt DESC
            """)
    List<Object[]> findAllWithTypeNames();

    @Query("""
                SELECT c, ct.uid,
                       (SELECT COUNT(e) FROM ComponentEntry e WHERE e.componentId = c.id)
                FROM Component c
                LEFT JOIN ComponentType ct ON c.componentTypeId = ct.id
                ORDER BY c.updatedAt DESC
            """)
    List<Object[]> findAllWithTypeNamesAndEntryCount();

    @Query(value = """
                SELECT c, ct.uid,
                       (SELECT COUNT(e) FROM ComponentEntry e WHERE e.componentId = c.id)
                FROM Component c
                LEFT JOIN ComponentType ct ON c.componentTypeId = ct.id
            """, countQuery = """
                SELECT COUNT(c)
                FROM Component c
            """)
    Page<Object[]> findAllPagedWithTypeNamesAndEntryCount(Pageable pageable);

    @Query(value = """
                SELECT c, ct.uid,
                       (SELECT COUNT(e) FROM ComponentEntry e WHERE e.componentId = c.id)
                FROM Component c
                LEFT JOIN ComponentType ct ON c.componentTypeId = ct.id
                WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(c.uid) LIKE LOWER(CONCAT('%', :query, '%'))
            """, countQuery = """
                SELECT COUNT(c)
                FROM Component c
                WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(c.uid) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<Object[]> searchByQueryWithTypeNamesAndEntryCount(@Param("query") String query, Pageable pageable);

    @Query("SELECT c FROM Component c WHERE c.id = :id")
    Optional<Component> findByIdOptimized(@Param("id") Long id);

    List<Component> findByUidInAndStatus(List<String> uids, ComponentStatus status);

    List<Component> findByUidInAndStatusIn(List<String> uids, java.util.Collection<ComponentStatus> statuses);

    List<Component> findByIdIn(List<Long> ids);

    @Query("SELECT c FROM Component c WHERE c.responsiveMedia.id = :responsiveMediaId")
    List<Component> findByResponsiveMediaId(@Param("responsiveMediaId") Long responsiveMediaId);

    boolean existsByNavigationNodeIdIn(List<Long> navigationNodeIds);

    long countByStatus(ComponentStatus status);

    long countByCreatedAtAfter(LocalDateTime date);

    @Query("""
                SELECT c
                FROM Component c
                WHERE c.status = com.backend.domain.enums.ComponentStatus.PUBLISHED
                  AND (
                    cast(function('JSON_UNQUOTE', function('JSON_EXTRACT', c.customData, '$.layoutRole')) as string) LIKE 'header.%'
                    OR cast(function('JSON_UNQUOTE', function('JSON_EXTRACT', c.customData, '$.layoutRole')) as string) LIKE 'footer.%'
                  )
                ORDER BY c.displayOrder ASC
                """)
    List<Component> findPublishedShellComponents();
}
