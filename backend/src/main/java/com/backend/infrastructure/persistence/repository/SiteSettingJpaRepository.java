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

  // Tenant-agnostic queries (database-per-tenant isolation)
  Optional<SiteSetting> findBySettingKeyAndLanguage(String settingKey, Language language);

  Optional<SiteSetting> findBySettingKeyAndLanguageIsNull(String settingKey);

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
