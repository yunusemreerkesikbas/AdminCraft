package com.backend.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.command.PageTemplateCommands.CreatePageTemplateCommand;
import com.backend.application.command.PageTemplateCommands.CreateTemplateSlotCommand;
import com.backend.application.command.PageTemplateCommands.ReorderTemplateSlotsCommand;
import com.backend.application.command.PageTemplateCommands.UpdatePageTemplateCommand;
import com.backend.application.dto.template.PageTemplateDto;
import com.backend.application.dto.template.TemplateSlotDto;
import com.backend.application.mapper.PageTemplateMapper;
import com.backend.domain.entity.Page;
import com.backend.domain.entity.PageSlot;
import com.backend.domain.entity.PageTemplate;
import com.backend.domain.entity.PageTemplateI18n;
import com.backend.domain.entity.TemplateSlot;
import com.backend.domain.enums.Language;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.domain.repository.PageRepository;
import com.backend.domain.repository.PageSlotRepository;
import com.backend.domain.repository.PageTemplateI18nRepository;
import com.backend.domain.repository.PageTemplateRepository;
import com.backend.domain.repository.TemplateSlotRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PageTemplateServiceImpl implements PageTemplateService {

  private static final Language DEFAULT_LANGUAGE = Language.TR;

  private final PageTemplateRepository pageTemplateRepository;
  private final TemplateSlotRepository templateSlotRepository;
  private final PageRepository pageRepository;
  private final PageSlotRepository pageSlotRepository;
  private final PageTemplateI18nRepository templateI18nRepository;
  private final PageTemplateMapper pageTemplateMapper;

  @Override
  @Transactional(readOnly = true)
  public List<PageTemplateDto> getAll() {
    return pageTemplateRepository.findAll().stream()
        .map(pageTemplateMapper::toDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public org.springframework.data.domain.Page<PageTemplateDto> getTemplates(Pageable pageable, String search) {
    if (search == null || search.trim().length() < 2) {
      return pageTemplateRepository.findAll(pageable, null)
          .map(pageTemplateMapper::toDto);
    }
    return pageTemplateRepository.findAll(pageable, search.trim())
        .map(pageTemplateMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PageTemplateDto> getActiveTemplates() {
    return pageTemplateRepository.findByIsActiveTrue().stream()
        .map(pageTemplateMapper::toDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public PageTemplateDto getById(Long id) {
    return pageTemplateRepository.findById(id)
        .map(pageTemplateMapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException("PageTemplate", id));
  }

  @Override
  public PageTemplateDto create(CreatePageTemplateCommand command) {
    if (pageTemplateRepository.existsByUid(command.uid())) {
      throw new IllegalArgumentException("Template with UID '" + command.uid() + "' already exists");
    }

    PageTemplate template = new PageTemplate();
    template.setUid(command.uid());
    template.setIsActive(command.isActive() != null ? command.isActive() : true);
    template.setIsSystem(false);

    PageTemplate saved = pageTemplateRepository.save(template);

    PageTemplateI18n i18n = new PageTemplateI18n(saved.getId(), getDefaultLanguage(), command.name(),
        command.description());
    templateI18nRepository.save(i18n);

    log.info("Created page template: {} ({})", command.name(), saved.getUid());

    return pageTemplateMapper.toDto(saved, command.name(), command.description());
  }

  @Override
  public PageTemplateDto update(Long id, UpdatePageTemplateCommand command) {
    PageTemplate template = pageTemplateRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("PageTemplate", id));

    if (Boolean.TRUE.equals(template.getIsSystem())) {
      throw new IllegalArgumentException("Cannot modify system template: " + template.getUid());
    }

    PageTemplateI18n i18n = templateI18nRepository.findByTemplateIdAndLanguage(id, getDefaultLanguage())
        .orElseGet(() -> {
          PageTemplateI18n newI18n = new PageTemplateI18n();
          newI18n.setTemplateId(id);
          newI18n.setLanguage(getDefaultLanguage());
          return newI18n;
        });

    String currentName = i18n.getName();
    if (command.name() != null) {
      i18n.setName(command.name());
      currentName = command.name();
    }
    if (command.description() != null) {
      i18n.setDescription(command.description());
    }
    templateI18nRepository.save(i18n);

    if (command.isActive() != null) {
      template.setIsActive(command.isActive());
    }

    PageTemplate saved = pageTemplateRepository.save(template);
    log.info("Updated page template: {} ({})", currentName, saved.getUid());

    return pageTemplateMapper.toDto(saved);
  }

  @Override
  public void delete(Long id) {
    PageTemplate template = pageTemplateRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("PageTemplate", id));

    if (Boolean.TRUE.equals(template.getIsSystem())) {
      throw new IllegalArgumentException("Cannot delete system template: " + template.getUid());
    }

    pageTemplateRepository.deleteById(id);
    log.info("Deleted page template: {}", template.getUid());
  }

  @Override
  public TemplateSlotDto addSlot(Long templateId, CreateTemplateSlotCommand command) {
    PageTemplate template = pageTemplateRepository.findById(templateId)
        .orElseThrow(() -> new EntityNotFoundException("PageTemplate", templateId));

    if (templateSlotRepository.existsByTemplateIdAndSlotName(templateId, command.slotName())) {
      throw new IllegalArgumentException(
          "Slot '" + command.slotName() + "' already exists in template");
    }

    TemplateSlot slot = new TemplateSlot();
    slot.setTemplateId(templateId);
    slot.setSlotName(command.slotName());
    slot.setPosition(command.position());
    slot.setSortOrder(command.sortOrder() != null ? command.sortOrder() : 0);
    slot.setIsRequired(command.isRequired() != null ? command.isRequired() : false);
    slot.setMaxComponents(command.maxComponents());

    if (command.allowedTypes() != null) {
      if (command.allowedTypes().stream().anyMatch(v -> v == null || v.isBlank())) {
        throw new IllegalArgumentException("allowedTypes contains blank value");
      }
      if (command.allowedTypes().stream().distinct().count() != command.allowedTypes().size()) {
        throw new IllegalArgumentException("allowedTypes contains duplicate value");
      }
    }
    slot.setAllowedTypes(pageTemplateMapper.encodeAllowedTypes(command.allowedTypes()));

    TemplateSlot saved = templateSlotRepository.save(slot);
    log.info("Added slot '{}' to template '{}'", command.slotName(), template.getUid());

    return pageTemplateMapper.toSlotDto(saved);
  }

  @Override
  public void removeSlot(Long templateId, String slotName) {
    TemplateSlot slot = templateSlotRepository.findByTemplateIdAndSlotName(templateId, slotName)
        .orElseThrow(() -> new EntityNotFoundException("TemplateSlot", slotName));

    templateSlotRepository.delete(slot);
    log.info("Removed slot '{}' from template {}", slotName, templateId);
  }

  @Override
  public void assignTemplateToPage(Long pageId, Long templateId) {
    Page page = pageRepository.findById(pageId)
        .orElseThrow(() -> new EntityNotFoundException("Page", pageId));

    if (templateId == null) {
      page.setTemplateId(null);
      pageRepository.save(page);
      log.info("Cleared template from page {}", pageId);
      return;
    }

    PageTemplate template = pageTemplateRepository.findById(templateId)
        .orElseThrow(() -> new EntityNotFoundException("PageTemplate", templateId));

    page.setTemplateId(templateId);
    pageRepository.save(page);

    // Get existing slot names in one query to avoid N+1
    Set<String> existingSlotNames = pageSlotRepository.findByPageId(pageId).stream()
        .map(PageSlot::getSlotName)
        .collect(Collectors.toSet());

    // Get template slots and filter to only create missing ones
    List<TemplateSlot> templateSlots = templateSlotRepository.findByTemplateId(templateId);
    List<PageSlot> newSlots = new ArrayList<>();

    for (TemplateSlot templateSlot : templateSlots) {
      if (!existingSlotNames.contains(templateSlot.getSlotName())) {
        PageSlot pageSlot = new PageSlot();
        pageSlot.setPageId(pageId);
        pageSlot.setSlotName(templateSlot.getSlotName());
        pageSlot.setPosition(templateSlot.getPosition());
        pageSlot.setSortOrder(templateSlot.getSortOrder());
        pageSlot.setIsActive(true);
        pageSlot.setIsShared(false);
        newSlots.add(pageSlot);
      }
    }

    // Batch save all new slots
    if (!newSlots.isEmpty()) {
      pageSlotRepository.saveAll(newSlots);
      log.debug("Created {} page slots for page {}", newSlots.size(), pageId);
    }

    log.info("Assigned template '{}' to page {} and created {} slots",
        template.getUid(), pageId, newSlots.size());
  }

  @Override
  public void reorderSlots(Long templateId, ReorderTemplateSlotsCommand command) {
    PageTemplate template = pageTemplateRepository.findById(templateId)
        .orElseThrow(() -> new EntityNotFoundException("PageTemplate", templateId));

    if (Boolean.TRUE.equals(template.getIsSystem())) {
      throw new IllegalArgumentException("Cannot modify system template: " + template.getUid());
    }

    List<TemplateSlot> slots = templateSlotRepository.findByTemplateId(templateId);

    var slotMap = slots.stream()
        .collect(Collectors.toMap(TemplateSlot::getSlotName, s -> s));

    List<TemplateSlot> updatedSlots = new ArrayList<>();
    for (int i = 0; i < command.slotNames().size(); i++) {
      String slotName = command.slotNames().get(i);
      TemplateSlot slot = slotMap.get(slotName);
      if (slot != null) {
        slot.setSortOrder(i);
        updatedSlots.add(slot);
      }
    }

    templateSlotRepository.saveAll(updatedSlots);
    log.info("Reordered {} slots for template {}", updatedSlots.size(), templateId);
  }

  private Language getDefaultLanguage() {
    // TODO: In future, can be fetched from tenant settings
    return DEFAULT_LANGUAGE;
  }
}
