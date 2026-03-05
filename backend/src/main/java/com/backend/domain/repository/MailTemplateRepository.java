package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.MailTemplate;

public interface MailTemplateRepository {

    Optional<MailTemplate> findById(Long id);

    List<MailTemplate> findByTemplateKeyIgnoreCaseOrderByLanguageAsc(String templateKey);

    Optional<MailTemplate> findByTemplateKeyIgnoreCaseAndLanguageIgnoreCase(String templateKey, String language);

    MailTemplate save(MailTemplate template);
}
