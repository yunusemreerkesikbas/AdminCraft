package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.PageI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.PageStatus;

@Repository
public interface PageI18nRepository extends JpaRepository<PageI18n, Long> {

        Optional<PageI18n> findByUuid(String uuid);

        Optional<PageI18n> findByUid(String uid);

        Optional<PageI18n> findByPageIdAndLanguage(Long pageId, Language language);

        Optional<PageI18n> findByLanguageAndCanonicalUrl(Language language, String canonicalUrl);

        List<PageI18n> findByPageId(Long pageId);

        List<PageI18n> findByLanguage(Language language);

        @Query("SELECT pi FROM PageI18n pi WHERE pi.language = :language AND pi.canonicalUrl = :canonicalUrl AND pi.status = 'PUBLISHED'")
        Optional<PageI18n> findPublishedByCanonicalUrl(@Param("language") Language language,
                        @Param("canonicalUrl") String canonicalUrl);

        List<PageI18n> findByLanguageAndStatus(Language language, PageStatus status);

        boolean existsByPageIdAndLanguage(Long pageId, Language language);

        long countByPageId(Long pageId);

        void deleteByPageId(Long pageId);

        @Query("SELECT pi FROM PageI18n pi, Page p WHERE pi.pageId = p.id AND pi.language = :language AND pi.status = 'PUBLISHED' ORDER BY p.publishedAt DESC")
        List<PageI18n> findPublishedByLanguage(@Param("language") Language language);

        @Query("SELECT pi FROM PageI18n pi, Page p WHERE pi.pageId = p.id AND pi.status = 'SCHEDULED' ORDER BY p.scheduledAt ASC")
        List<PageI18n> findScheduled();

        @Query("SELECT pi FROM PageI18n pi, Page p WHERE pi.pageId = p.id AND pi.status = 'SCHEDULED' AND p.scheduledAt <= CURRENT_TIMESTAMP")
        List<PageI18n> findReadyForPublication();

        long countByLanguageAndStatus(Language language, PageStatus status);

        List<PageI18n> findByPageIdInAndLanguage(List<Long> pageIds, Language language);
}
