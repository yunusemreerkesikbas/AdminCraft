package com.backend.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.request.NavigationEntryI18nRequest;
import com.backend.application.dto.request.NavigationNodeI18nRequest;
import com.backend.application.dto.response.NavigationEntryI18nResponse;
import com.backend.application.dto.response.NavigationNodeI18nResponse;
import com.backend.domain.entity.NavigationEntryI18n;
import com.backend.domain.entity.NavigationNodeI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.domain.repository.NavigationEntryI18nRepository;
import com.backend.domain.repository.NavigationEntryRepository;
import com.backend.domain.repository.NavigationNodeI18nRepository;
import com.backend.domain.repository.NavigationNodeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NavigationI18nServiceImpl implements NavigationI18nService {

  private static final Language DEFAULT_LANGUAGE = Language.TR;

  private final NavigationNodeRepository nodeRepository;
  private final NavigationEntryRepository entryRepository;
  private final NavigationNodeI18nRepository nodeI18nRepository;
  private final NavigationEntryI18nRepository entryI18nRepository;

  // ==================== Node i18n Operations ====================

  @Override
  @Transactional(readOnly = true)
  public NavigationNodeI18nResponse getNodeI18n(Long nodeId, Language language) {
    validateNodeExists(nodeId);

    NavigationNodeI18n i18n = nodeI18nRepository.findByNodeIdAndLanguage(nodeId, language)
        .orElseGet(() -> getFallbackNodeI18n(nodeId));

    return NavigationNodeI18nResponse.from(i18n);
  }

  @Override
  public NavigationNodeI18nResponse upsertNodeI18n(Long nodeId, Language language, NavigationNodeI18nRequest request) {
    validateNodeExists(nodeId);

    NavigationNodeI18n i18n = nodeI18nRepository.findByNodeIdAndLanguage(nodeId, language)
        .orElseGet(() -> {
          NavigationNodeI18n newI18n = new NavigationNodeI18n();
          newI18n.setNodeId(nodeId);
          newI18n.setLanguage(language);
          return newI18n;
        });

    if (request.title() != null) {
      i18n.setTitle(request.title());
    }

    NavigationNodeI18n saved = nodeI18nRepository.save(i18n);
    log.info("Upserted navigation node i18n: nodeId={}, language={}", nodeId, language);

    return NavigationNodeI18nResponse.from(saved);
  }

  // ==================== Entry i18n Operations ====================

  @Override
  @Transactional(readOnly = true)
  public NavigationEntryI18nResponse getEntryI18n(Long entryId, Language language) {
    validateEntryExists(entryId);

    NavigationEntryI18n i18n = entryI18nRepository.findByEntryIdAndLanguage(entryId, language)
        .orElseGet(() -> getFallbackEntryI18n(entryId));

    return NavigationEntryI18nResponse.from(i18n);
  }

  @Override
  public NavigationEntryI18nResponse upsertEntryI18n(Long entryId, Language language,
      NavigationEntryI18nRequest request) {
    validateEntryExists(entryId);

    NavigationEntryI18n i18n = entryI18nRepository.findByEntryIdAndLanguage(entryId, language)
        .orElseGet(() -> {
          NavigationEntryI18n newI18n = new NavigationEntryI18n();
          newI18n.setEntryId(entryId);
          newI18n.setLanguage(language);
          return newI18n;
        });

    if (request.linkName() != null) {
      i18n.setLinkName(request.linkName());
    }

    NavigationEntryI18n saved = entryI18nRepository.save(i18n);
    log.info("Upserted navigation entry i18n: entryId={}, language={}", entryId, language);

    return NavigationEntryI18nResponse.from(saved);
  }

  // ==================== Private Helpers ====================

  private void validateNodeExists(Long nodeId) {
    if (nodeRepository.findById(nodeId).isEmpty()) {
      throw new EntityNotFoundException("NavigationNode", nodeId);
    }
  }

  private void validateEntryExists(Long entryId) {
    if (entryRepository.findById(entryId).isEmpty()) {
      throw new EntityNotFoundException("NavigationEntry", entryId);
    }
  }

  /**
   * Fallback logic: Default Language → First Available
   * Throws EntityNotFoundException if no i18n records exist
   */
  private NavigationNodeI18n getFallbackNodeI18n(Long nodeId) {
    // Try default language first
    return nodeI18nRepository.findByNodeIdAndLanguage(nodeId, DEFAULT_LANGUAGE)
        .orElseGet(() -> {
          // Get first available
          var allI18n = nodeI18nRepository.findByNodeId(nodeId);
          if (allI18n.isEmpty()) {
            throw new EntityNotFoundException("NavigationNodeI18n for node ID: " + nodeId);
          }
          return allI18n.get(0);
        });
  }

  /**
   * Fallback logic: Default Language → First Available
   * Throws EntityNotFoundException if no i18n records exist
   */
  private NavigationEntryI18n getFallbackEntryI18n(Long entryId) {
    // Try default language first
    return entryI18nRepository.findByEntryIdAndLanguage(entryId, DEFAULT_LANGUAGE)
        .orElseGet(() -> {
          // Get first available
          var allI18n = entryI18nRepository.findByEntryId(entryId);
          if (allI18n.isEmpty()) {
            throw new EntityNotFoundException("NavigationEntryI18n for entry ID: " + entryId);
          }
          return allI18n.get(0);
        });
  }
}
