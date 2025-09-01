package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.SiteSetting;
import com.backend.domain.enums.Language;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteSettingJpaRepository extends JpaRepository<SiteSetting, Long> {

  // Tenant-isolated queries
  Optional<SiteSetting> findByTenantIdAndSettingKeyAndLanguage(Long tenantId, String settingKey, Language language);

  Optional<SiteSetting> findByTenantIdAndSettingKeyAndLanguageIsNull(Long tenantId, String settingKey);

  List<SiteSetting> findByTenantIdAndLanguage(Long tenantId, Language language);

  List<SiteSetting> findByTenantIdAndLanguageIsNull(Long tenantId);

  List<SiteSetting> findByTenantId(Long tenantId);

  // Batch operations for N+1 prevention
  @Query("SELECT s FROM SiteSetting s WHERE s.tenantId = :tenantId AND s.settingKey IN :keys")
  List<SiteSetting> findByTenantIdAndSettingKeyIn(@Param("tenantId") Long tenantId, @Param("keys") List<String> keys);

  @Query("SELECT s FROM SiteSetting s WHERE s.tenantId = :tenantId AND s.language IN :languages")
  List<SiteSetting> findByTenantIdAndLanguageIn(@Param("tenantId") Long tenantId, @Param("languages") List<Language> languages);

  // Public settings for site frontend
  List<SiteSetting> findByTenantIdAndIsPublicTrue(Long tenantId);

  List<SiteSetting> findByTenantIdAndLanguageAndIsPublicTrue(Long tenantId, Language language);
}
