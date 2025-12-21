package com.backend.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.request.PageTemplateI18nRequest;
import com.backend.application.dto.response.PageTemplateI18nResponse;
import com.backend.domain.entity.PageTemplateI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.domain.repository.PageTemplateI18nRepository;
import com.backend.domain.repository.PageTemplateRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PageTemplateI18nServiceImpl implements PageTemplateI18nService {

  private static final Language DEFAULT_LANGUAGE = Language.TR;

  private final PageTemplateRepository templateRepository;
  private final PageTemplateI18nRepository templateI18nRepository;

  @Override
  @Transactional(readOnly = true)
  public PageTemplateI18nResponse getTemplateI18n(Long templateId, Language language) {
    validateTemplateExists(templateId);

    PageTemplateI18n i18n = templateI18nRepository.findByTemplateIdAndLanguage(templateId, language)
        .orElseGet(() -> getFallbackTemplateI18n(templateId));

    return PageTemplateI18nResponse.from(i18n);
  }

  @Override
  public PageTemplateI18nResponse upsertTemplateI18n(Long templateId, Language language,
      PageTemplateI18nRequest request) {
    validateTemplateExists(templateId);

    PageTemplateI18n i18n = templateI18nRepository.findByTemplateIdAndLanguage(templateId, language)
        .orElseGet(() -> {
          PageTemplateI18n newI18n = new PageTemplateI18n();
          newI18n.setTemplateId(templateId);
          newI18n.setLanguage(language);
          return newI18n;
        });

    if (request.name() != null) {
      i18n.setName(request.name());
    } else if (i18n.getId() == null) {
      throw new IllegalArgumentException("Name is required for new PageTemplateI18n");
    }
    if (request.description() != null) {
      i18n.setDescription(request.description());
    }

    PageTemplateI18n saved = templateI18nRepository.save(i18n);
    log.info("Upserted page template i18n: templateId={}, language={}", templateId, language);

    return PageTemplateI18nResponse.from(saved);
  }

  // ==================== Private Helpers ====================

  private void validateTemplateExists(Long templateId) {
    if (templateRepository.findById(templateId).isEmpty()) {
      throw new EntityNotFoundException("PageTemplate", templateId);
    }
  }

  /**
   * Fallback logic: Default Language → First Available
   * Throws EntityNotFoundException if no i18n records exist
   */
  private PageTemplateI18n getFallbackTemplateI18n(Long templateId) {
    // Try default language first
    return templateI18nRepository.findByTemplateIdAndLanguage(templateId, DEFAULT_LANGUAGE)
        .orElseGet(() -> {
          // Get first available
          var allI18n = templateI18nRepository.findByTemplateId(templateId);
          if (allI18n.isEmpty()) {
            throw new EntityNotFoundException("PageTemplateI18n for template ID: " + templateId);
          }
          return allI18n.get(0);
        });
  }
}
