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

  @Query("SELECT s FROM SiteSetting s WHERE s.settingKey = :key AND s.language = :language")
  Optional<SiteSetting> findByKeyAndLanguage(@Param("key") String key,
      @Param("language") Language language);

  @Query("SELECT s FROM SiteSetting s WHERE s.settingKey = :key AND s.language IS NULL")
  Optional<SiteSetting> findGlobalByKey(@Param("key") String key);

  List<SiteSetting> findByLanguage(Language language);

  List<SiteSetting> findByLanguageIsNull();
}
