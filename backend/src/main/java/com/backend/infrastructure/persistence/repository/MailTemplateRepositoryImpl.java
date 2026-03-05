package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.backend.domain.entity.MailTemplate;
import com.backend.domain.repository.MailTemplateRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MailTemplateRepositoryImpl implements MailTemplateRepository {

    private final MailTemplateJpaRepository jpaRepository;

    @Override
    public Optional<MailTemplate> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<MailTemplate> findByTemplateKeyIgnoreCaseOrderByLanguageAsc(String templateKey) {
        return jpaRepository.findByTemplateKeyIgnoreCaseOrderByLanguageAsc(templateKey);
    }

    @Override
    public Optional<MailTemplate> findByTemplateKeyIgnoreCaseAndLanguageIgnoreCase(String templateKey, String language) {
        return jpaRepository.findByTemplateKeyIgnoreCaseAndLanguageIgnoreCase(templateKey, language);
    }

    @Override
    public MailTemplate save(MailTemplate template) {
        return jpaRepository.save(template);
    }
}
