package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.backend.domain.entity.PlatformEmailTemplate;
import com.backend.domain.repository.PlatformEmailTemplateRepository;
import com.backend.infrastructure.persistence.platform.mapper.PlatformMailMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlatformEmailTemplatePersistenceAdapter implements PlatformEmailTemplateRepository {

    private final com.backend.infrastructure.persistence.platform.repository.PlatformEmailTemplateRepository jpaRepository;

    @Override
    public Optional<PlatformEmailTemplate> findById(Long id) {
        return jpaRepository.findById(id).map(PlatformMailMapper::toDomain);
    }

    @Override
    public List<PlatformEmailTemplate> findByTemplateKeyIgnoreCaseOrderByLanguageAsc(String templateKey) {
        return jpaRepository.findByTemplateKeyIgnoreCaseOrderByLanguageAsc(templateKey).stream()
                .map(PlatformMailMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PlatformEmailTemplate> findByTemplateKeyIgnoreCaseAndLanguageIgnoreCase(String templateKey, String language) {
        return jpaRepository.findByTemplateKeyIgnoreCaseAndLanguageIgnoreCase(templateKey, language)
                .map(PlatformMailMapper::toDomain);
    }

    @Override
    public PlatformEmailTemplate save(PlatformEmailTemplate template) {
        com.backend.infrastructure.persistence.platform.entity.PlatformEmailTemplate entity =
                template.getId() == null
                        ? new com.backend.infrastructure.persistence.platform.entity.PlatformEmailTemplate()
                        : jpaRepository.findById(template.getId())
                                .orElseThrow(() -> new IllegalArgumentException("mail.marketing.template.not.found"));
        entity.setId(template.getId());
        entity.setTemplateKey(template.getTemplateKey());
        entity.setLanguage(template.getLanguage());
        entity.setSubject(template.getSubject());
        entity.setContent(template.getContent());
        entity.setIsActive(template.getIsActive());
        return PlatformMailMapper.toDomain(jpaRepository.save(entity));
    }
}
