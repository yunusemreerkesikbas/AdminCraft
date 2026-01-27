package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.SiteTechnicalSettings;

@Repository
public interface SiteTechnicalSettingsJpaRepository extends JpaRepository<SiteTechnicalSettings, Long> {

    Optional<SiteTechnicalSettings> findBySiteId(Long siteId);

    boolean existsBySiteId(Long siteId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteBySiteId(Long siteId);

    List<SiteTechnicalSettings> findByIndexingEnabledTrue();

    List<SiteTechnicalSettings> findBySitemapEnabledTrue();

    List<SiteTechnicalSettings> findByCookieConsentEnabledTrue();
}
