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
    return jpa.findByTenantIdAndSettingKeyAndLanguage(tenantId, key, language);
  }

  @Override
  public Optional<SiteSetting> findByTenantIdAndSettingKeyAndLanguageIsNull(Long tenantId, String key) {
    return jpa.findByTenantIdAndSettingKeyAndLanguageIsNull(tenantId, key);
  }

  @Override
  public List<SiteSetting> findByTenantIdAndLanguage(Long tenantId, Language language) {
    return jpa.findByTenantIdAndLanguage(tenantId, language);
  }

  @Override
  public List<SiteSetting> findByTenantIdAndLanguageIsNull(Long tenantId) {
    return jpa.findByTenantIdAndLanguageIsNull(tenantId);
  }

  @Override
  public List<SiteSetting> findByTenantId(Long tenantId) {
    return jpa.findByTenantId(tenantId);
  }

  @Override
  public List<SiteSetting> findByTenantIdAndSettingKeyIn(Long tenantId, List<String> keys) {
    return jpa.findByTenantIdAndSettingKeyIn(tenantId, keys);
  }

  @Override
  public List<SiteSetting> findByTenantIdAndLanguageIn(Long tenantId, List<Language> languages) {
    return jpa.findByTenantIdAndLanguageIn(tenantId, languages);
  }

  @Override
  public List<SiteSetting> findByTenantIdAndIsPublicTrue(Long tenantId) {
    return jpa.findByTenantIdAndIsPublicTrue(tenantId);
  }

  @Override
  public List<SiteSetting> findByTenantIdAndLanguageAndIsPublicTrue(Long tenantId, Language language) {
    return jpa.findByTenantIdAndLanguageAndIsPublicTrue(tenantId, language);
  }
}
