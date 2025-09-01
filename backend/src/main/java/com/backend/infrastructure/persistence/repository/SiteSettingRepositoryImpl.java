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
  public Optional<SiteSetting> findBySettingKeyAndLanguage(String key, Language language) {
    return jpa.findByKeyAndLanguage(key, language);
  }

  @Override
  public Optional<SiteSetting> findBySettingKeyAndLanguageIsNull(String key) {
    return jpa.findGlobalByKey(key);
  }

  @Override
  public List<SiteSetting> findByLanguage(Language language) {
    return jpa.findByLanguage(language);
  }

  @Override
  public List<SiteSetting> findByLanguageIsNull() {
    return jpa.findByLanguageIsNull();
  }

  @Override
  public List<SiteSetting> findAll() {
    return jpa.findAll();
  }
}
