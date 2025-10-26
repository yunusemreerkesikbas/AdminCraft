package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.SiteSetting;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.SiteSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SiteSettingRepositoryImpl implements SiteSettingRepository {

  private final SiteSettingJpaRepository jpa;

  @Override
  public SiteSetting save(SiteSetting setting) {
    return jpa.save(setting);
  }

  @Override
  public List<SiteSetting> saveAll(Iterable<SiteSetting> settings) {
    return jpa.saveAll(settings);
  }

  @Override
  public Optional<SiteSetting> findByTenantIdAndSettingKeyAndLanguage(Long tenantId, String key, Language language) {
    // In database-per-tenant model, routing is handled by TenantContext
    return jpa.findBySettingKeyAndLanguage(key, language);
  }

  @Override
  public Optional<SiteSetting> findByTenantIdAndSettingKeyAndLanguageIsNull(Long tenantId, String key) {
    // In database-per-tenant model, routing is handled by TenantContext
    return jpa.findBySettingKeyAndLanguageIsNull(key);
  }

  @Override
  public List<SiteSetting> findByTenantIdAndLanguage(Long tenantId, Language language) {
    // In database-per-tenant model, routing is handled by TenantContext
    return jpa.findByLanguage(language);
  }

  @Override
  public List<SiteSetting> findByTenantIdAndLanguageIsNull(Long tenantId) {
    // In database-per-tenant model, routing is handled by TenantContext
    return jpa.findByLanguageIsNull();
  }

  @Override
  public List<SiteSetting> findByTenantId(Long tenantId) {
    // In database-per-tenant model, all settings in current DB belong to tenant
    return jpa.findAll();
  }

  @Override
  public List<SiteSetting> findByTenantIdAndSettingKeyIn(Long tenantId, List<String> keys) {
    // In database-per-tenant model, routing is handled by TenantContext
    return jpa.findBySettingKeyIn(keys);
  }

  @Override
  public List<SiteSetting> findByTenantIdAndLanguageIn(Long tenantId, List<Language> languages) {
    // In database-per-tenant model, routing is handled by TenantContext
    return jpa.findByLanguageIn(languages);
  }

  @Override
  public List<SiteSetting> findByTenantIdAndIsPublicTrue(Long tenantId) {
    // In database-per-tenant model, routing is handled by TenantContext
    return jpa.findByIsPublicTrue();
  }

  @Override
  public List<SiteSetting> findByTenantIdAndLanguageAndIsPublicTrue(Long tenantId, Language language) {
    // In database-per-tenant model, routing is handled by TenantContext
    return jpa.findByLanguageAndIsPublicTrue(language);
  }
}
