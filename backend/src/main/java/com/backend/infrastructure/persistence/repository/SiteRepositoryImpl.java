package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Site;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SiteRepositoryImpl implements SiteRepository {

    private final SiteJpaRepository siteJpaRepository;

    @Override
    public Site save(Site site) {
        return siteJpaRepository.save(site);
    }

    @Override
    public List<Site> saveAll(Iterable<Site> sites) {
        return siteJpaRepository.saveAll(sites);
    }

    @Override
    public Optional<Site> findById(Long id) {
        return siteJpaRepository.findById(id);
    }

    @Override
    public List<Site> findAll() {
        return siteJpaRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        siteJpaRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        siteJpaRepository.deleteAll();
    }

    @Override
    public boolean existsById(Long id) {
        return siteJpaRepository.existsById(id);
    }

    @Override
    public long count() {
        return siteJpaRepository.count();
    }

    @Override
    public void deleteAllById(Iterable<Long> ids) {
        siteJpaRepository.deleteAllById(ids);
    }

    @Override
    public List<Site> findAllByOrderByCreatedAtDesc() {
        return siteJpaRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public Optional<Site> findByDomain(String domain) {
        return siteJpaRepository.findByDomain(domain);
    }

    @Override
    public Optional<Site> findByDomainIgnoreCase(String domain) {
        return siteJpaRepository.findByDomainIgnoreCase(domain);
    }

    @Override
    public Optional<Site> findByCustomDomain(String customDomain) {
        return siteJpaRepository.findByCustomDomain(customDomain);
    }

    @Override
    public boolean existsByDomain(String domain) {
        return siteJpaRepository.existsByDomain(domain);
    }

    @Override
    public boolean existsByDomainIgnoreCase(String domain) {
        return siteJpaRepository.existsByDomainIgnoreCase(domain);
    }

    @Override
    public boolean existsByCustomDomain(String customDomain) {
        return siteJpaRepository.existsByCustomDomain(customDomain);
    }

    @Override
    public boolean existsByCustomDomainIgnoreCase(String customDomain) {
        return siteJpaRepository.existsByCustomDomainIgnoreCase(customDomain);
    }

    @Override
    public boolean existsBySiteNameIgnoreCase(String siteName) {
        return siteJpaRepository.existsBySiteNameIgnoreCase(siteName);
    }

    @Override
    public List<Site> findByPublishedTrue() {
        return siteJpaRepository.findByPublishedTrue();
    }

    @Override
    public List<Site> findByPublishedFalse() {
        return siteJpaRepository.findByPublishedFalse();
    }

    @Override
    public List<Site> findByMaintenanceModeTrue() {
        return siteJpaRepository.findByMaintenanceModeTrue();
    }

    @Override
    public List<Site> findByEnabledLanguagesContaining(Language language) {
        return siteJpaRepository.findByEnabledLanguagesContaining(language);
    }

    @Override
    public List<Site> findByDefaultLanguage(Language language) {
        return siteJpaRepository.findByDefaultLanguage(language);
    }

    @Override
    public List<Site> findByThemeName(String themeName) {
        return siteJpaRepository.findByThemeName(themeName);
    }

    @Override
    public List<Site> findBySslEnabledTrue() {
        return siteJpaRepository.findBySslEnabledTrue();
    }

    @Override
    public List<Site> findBySslEnabledFalse() {
        return siteJpaRepository.findBySslEnabledFalse();
    }

    @Override
    public List<Site> findBySiteNameContainingIgnoreCase(String siteName) {
        return siteJpaRepository.findBySiteNameContainingIgnoreCase(siteName);
    }

    @Override
    public List<Site> findByDescriptionContainingIgnoreCase(String description) {
        return siteJpaRepository.findByDescriptionContainingIgnoreCase(description);
    }

    @Override
    public List<Site> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return siteJpaRepository.findByCreatedAtBetween(startDate, endDate);
    }

    @Override
    public List<Site> findByPublishedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return siteJpaRepository.findByPublishedAtBetween(startDate, endDate);
    }

    @Override
    public List<Site> findWithAnalytics() {
        return siteJpaRepository.findWithAnalytics();
    }

    @Override
    public List<Site> findWithTagManager() {
        return siteJpaRepository.findWithTagManager();
    }

    @Override
    public List<Site> findActiveSites() {
        return siteJpaRepository.findActiveSites();
    }

    @Override
    public List<Site> findAllActiveSites() {
        return siteJpaRepository.findAllActiveSites();
    }

    @Override
    public long countPublishedSites() {
        return siteJpaRepository.countPublishedSites();
    }

    @Override
    public List<Site> findRecentlyPublishedSites() {
        return siteJpaRepository.findRecentlyPublishedSites();
    }

    @Override
    public List<Site> findByIdIn(List<Long> ids) {
        return siteJpaRepository.findByIdIn(ids);
    }
}