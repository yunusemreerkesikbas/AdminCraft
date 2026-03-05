package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.MailTemplate;

@Repository
public interface MailTemplateJpaRepository extends JpaRepository<MailTemplate, Long> {

    List<MailTemplate> findAllByOrderByTemplateKeyAscLanguageAsc();

    List<MailTemplate> findByTemplateKeyIgnoreCaseOrderByLanguageAsc(String templateKey);

    Optional<MailTemplate> findByTemplateKeyIgnoreCaseAndLanguageIgnoreCase(String templateKey, String language);
}
