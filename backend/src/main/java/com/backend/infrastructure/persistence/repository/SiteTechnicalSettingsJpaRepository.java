package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.SiteTechnicalSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteTechnicalSettingsJpaRepository extends JpaRepository<SiteTechnicalSettings, Long> {

    Optional<SiteTechnicalSettings> findBySiteId(Long siteId);

    boolean existsBySiteId(Long siteId);

    void deleteBySiteId(Long siteId);

    List<SiteTechnicalSettings> findByIndexingEnabledTrue();

    List<SiteTechnicalSettings> findBySitemapEnabledTrue();

    List<SiteTechnicalSettings> findByCookieConsentEnabledTrue();
}
