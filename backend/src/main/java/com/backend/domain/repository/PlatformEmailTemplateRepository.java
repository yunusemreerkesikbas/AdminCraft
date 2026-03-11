package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.PlatformEmailTemplate;

public interface PlatformEmailTemplateRepository {

    Optional<PlatformEmailTemplate> findById(Long id);

    List<PlatformEmailTemplate> findByTemplateKeyIgnoreCaseOrderByLanguageAsc(String templateKey);

    Optional<PlatformEmailTemplate> findByTemplateKeyIgnoreCaseAndLanguageIgnoreCase(String templateKey, String language);

    PlatformEmailTemplate save(PlatformEmailTemplate template);
}
