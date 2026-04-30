package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.SiteSetting;
import com.backend.domain.enums.Language;

@Repository
public interface SiteSettingJpaRepository extends JpaRepository<SiteSetting, Long> {

  // Tenant-agnostic queries (database-per-tenant isolation)
  List<SiteSetting> findBySettingKeyAndLanguage(String settingKey, Language language);

  List<SiteSetting> findBySettingKeyAndLanguageIsNull(String settingKey);

  List<SiteSetting> findByLanguage(Language language);

  List<SiteSetting> findByLanguageIsNull();

  // Batch operations for N+1 prevention
  @Query("SELECT s FROM SiteSetting s WHERE s.settingKey IN :keys")
  List<SiteSetting> findBySettingKeyIn(@Param("keys") List<String> keys);

  @Query("SELECT s FROM SiteSetting s WHERE s.language IN :languages")
  List<SiteSetting> findByLanguageIn(@Param("languages") List<Language> languages);

  // Public settings for site frontend
  List<SiteSetting> findByIsPublicTrue();

  List<SiteSetting> findByLanguageAndIsPublicTrue(Language language);
}
