package com.backend.domain.repository;

import com.backend.domain.entity.SiteSetting;
import com.backend.domain.enums.Language;

import java.util.List;
import java.util.Optional;

public interface SiteSettingRepository {

  SiteSetting save(SiteSetting setting);

  List<SiteSetting> saveAll(Iterable<SiteSetting> settings);

  Optional<SiteSetting> findBySettingKeyAndLanguage(String key, Language language);

  Optional<SiteSetting> findBySettingKeyAndLanguageIsNull(String key);

  List<SiteSetting> findByLanguage(Language language);

  List<SiteSetting> findByLanguageIsNull();

  List<SiteSetting> findAll();
}
