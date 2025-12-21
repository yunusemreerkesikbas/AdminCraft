package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.PageTemplateI18n;
import com.backend.domain.enums.Language;

@Repository
public interface PageTemplateI18nRepository extends JpaRepository<PageTemplateI18n, Long> {

  Optional<PageTemplateI18n> findByTemplateIdAndLanguage(Long templateId, Language language);

  List<PageTemplateI18n> findByTemplateId(Long templateId);

  List<PageTemplateI18n> findByTemplateIdIn(List<Long> templateIds);

  void deleteByTemplateId(Long templateId);

  boolean existsByTemplateIdAndLanguage(Long templateId, Language language);
}
