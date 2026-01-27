package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.SiteTechnicalSettings;
import com.backend.domain.repository.SiteTechnicalSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SiteTechnicalSettingsRepositoryImpl implements SiteTechnicalSettingsRepository {

    private final SiteTechnicalSettingsJpaRepository siteTechnicalSettingsJpaRepository;

    @Override
    public SiteTechnicalSettings save(SiteTechnicalSettings settings) {
        return siteTechnicalSettingsJpaRepository.save(settings);
    }

    @Override
    public Optional<SiteTechnicalSettings> findById(Long siteId) {
        return siteTechnicalSettingsJpaRepository.findById(siteId);
    }

    @Override
    public Optional<SiteTechnicalSettings> findBySiteId(Long siteId) {
        return siteTechnicalSettingsJpaRepository.findBySiteId(siteId);
    }

    @Override
    public boolean existsBySiteId(Long siteId) {
        return siteTechnicalSettingsJpaRepository.existsBySiteId(siteId);
    }

    @Override
    public void deleteById(Long siteId) {
        siteTechnicalSettingsJpaRepository.deleteById(siteId);
    }

    @Override
    public void deleteBySiteId(Long siteId) {
        siteTechnicalSettingsJpaRepository.deleteBySiteId(siteId);
    }

    @Override
    public List<SiteTechnicalSettings> findByIndexingEnabledTrue() {
        return siteTechnicalSettingsJpaRepository.findByIndexingEnabledTrue();
    }

    @Override
    public List<SiteTechnicalSettings> findBySitemapEnabledTrue() {
        return siteTechnicalSettingsJpaRepository.findBySitemapEnabledTrue();
    }

    @Override
    public List<SiteTechnicalSettings> findByCookieConsentEnabledTrue() {
        return siteTechnicalSettingsJpaRepository.findByCookieConsentEnabledTrue();
    }
}
