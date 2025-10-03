package com.backend.domain.repository;

import com.backend.domain.entity.PageI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.PageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageI18nRepository extends JpaRepository<PageI18n, Long> {

        Optional<PageI18n> findByUuid(String uuid);

        Optional<PageI18n> findByTenantIdAndUid(Long tenantId, String uid);

        Optional<PageI18n> findByTenantIdAndPageIdAndLanguage(Long tenantId, Long pageId, Language language);

        Optional<PageI18n> findByTenantIdAndLanguageAndUrlPath(Long tenantId, Language language, String urlPath);

        List<PageI18n> findByTenantIdAndPageId(Long tenantId, Long pageId);

        @Query("SELECT pi FROM PageI18n pi WHERE pi.tenantId = :tenantId AND pi.language = :language AND pi.urlPath = :urlPath AND pi.status = 'PUBLISHED'")
        Optional<PageI18n> findPublishedByUrl(@Param("tenantId") Long tenantId,
                        @Param("language") Language language,
                        @Param("urlPath") String urlPath);

        List<PageI18n> findByTenantIdAndLanguage(Long tenantId, Language language);

        List<PageI18n> findByTenantIdAndLanguageAndStatus(Long tenantId, Language language, PageStatus status);

        boolean existsByTenantIdAndPageIdAndLanguage(Long tenantId, Long pageId, Language language);

        boolean existsByPageIdAndLanguage(Long pageId, Language language);

        long countByPageId(Long pageId);

        void deleteByPageId(Long pageId);

        @Query("SELECT pi FROM PageI18n pi WHERE pi.tenantId = :tenantId AND pi.language = :language AND pi.status = 'PUBLISHED' ORDER BY pi.publishedAt DESC")
        List<PageI18n> findPublishedByTenantAndLanguage(@Param("tenantId") Long tenantId,
                        @Param("language") Language language);

        @Query("SELECT pi FROM PageI18n pi WHERE pi.tenantId = :tenantId AND pi.status = 'SCHEDULED' ORDER BY pi.scheduledAt ASC")
        List<PageI18n> findScheduledByTenant(@Param("tenantId") Long tenantId);

        @Query("SELECT pi FROM PageI18n pi WHERE pi.status = 'SCHEDULED' AND pi.scheduledAt <= CURRENT_TIMESTAMP")
        List<PageI18n> findReadyForPublication();

        long countByTenantIdAndLanguageAndStatus(Long tenantId, Language language, PageStatus status);
}
