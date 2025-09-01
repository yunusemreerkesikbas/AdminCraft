package com.backend.domain.repository;

import com.backend.domain.entity.SiteSetting;
import com.backend.domain.enums.Language;

import java.util.List;
import java.util.Optional;

public interface SiteSettingRepository {

  SiteSetting save(SiteSetting setting);

  List<SiteSetting> saveAll(Iterable<SiteSetting> settings);

  // Tenant-isolated queries
  Optional<SiteSetting> findByTenantIdAndSettingKeyAndLanguage(Long tenantId, String key, Language language);

  Optional<SiteSetting> findByTenantIdAndSettingKeyAndLanguageIsNull(Long tenantId, String key);

  List<SiteSetting> findByTenantIdAndLanguage(Long tenantId, Language language);

  List<SiteSetting> findByTenantIdAndLanguageIsNull(Long tenantId);

  List<SiteSetting> findByTenantId(Long tenantId);

  // Batch operations for performance
  List<SiteSetting> findByTenantIdAndSettingKeyIn(Long tenantId, List<String> keys);

  List<SiteSetting> findByTenantIdAndLanguageIn(Long tenantId, List<Language> languages);

  // For public settings (used by site frontend)
  List<SiteSetting> findByTenantIdAndIsPublicTrue(Long tenantId);

  List<SiteSetting> findByTenantIdAndLanguageAndIsPublicTrue(Long tenantId, Language language);
}
