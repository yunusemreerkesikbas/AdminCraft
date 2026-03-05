package com.backend.infrastructure.persistence.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.infrastructure.persistence.platform.entity.PlatformEmailTemplate;

@Repository
public interface PlatformEmailTemplateRepository extends JpaRepository<PlatformEmailTemplate, Long> {

    List<PlatformEmailTemplate> findAllByOrderByTemplateKeyAscLanguageAsc();

    List<PlatformEmailTemplate> findByTemplateKeyIgnoreCaseOrderByLanguageAsc(String templateKey);

    Optional<PlatformEmailTemplate> findByTemplateKeyIgnoreCaseAndLanguageIgnoreCase(String templateKey, String language);
}
