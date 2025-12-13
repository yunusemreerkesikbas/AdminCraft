package com.backend.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.command.PageTemplateCommands.CreatePageTemplateCommand;
import com.backend.application.command.PageTemplateCommands.CreateTemplateSlotCommand;
import com.backend.application.command.PageTemplateCommands.UpdatePageTemplateCommand;
import com.backend.application.dto.template.PageTemplateDto;
import com.backend.application.dto.template.TemplateSlotDto;
import com.backend.application.mapper.PageTemplateMapper;
import com.backend.domain.entity.Page;
import com.backend.domain.entity.PageSlot;
import com.backend.domain.entity.PageTemplate;
import com.backend.domain.entity.TemplateSlot;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.domain.repository.PageRepository;
import com.backend.domain.repository.PageSlotRepository;
import com.backend.domain.repository.PageTemplateRepository;
import com.backend.domain.repository.TemplateSlotRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PageTemplateServiceImpl implements PageTemplateService {

  private final PageTemplateRepository pageTemplateRepository;
  private final TemplateSlotRepository templateSlotRepository;
  private final PageRepository pageRepository;
  private final PageSlotRepository pageSlotRepository;
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
    template.setName(command.name());
    template.setUid(command.uid());
    template.setDescription(command.description());
    template.setIsActive(command.isActive() != null ? command.isActive() : true);
    template.setIsSystem(false);

    PageTemplate saved = pageTemplateRepository.save(template);
    log.info("Created page template: {} ({})", saved.getName(), saved.getUid());

    return pageTemplateMapper.toDto(saved);
  }

  @Override
  public PageTemplateDto update(Long id, UpdatePageTemplateCommand command) {
    PageTemplate template = pageTemplateRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("PageTemplate", id));

    if (Boolean.TRUE.equals(template.getIsSystem())) {
      throw new IllegalArgumentException("Cannot modify system template: " + template.getName());
    }

    if (command.name() != null) {
      template.setName(command.name());
    }
    if (command.description() != null) {
      template.setDescription(command.description());
    }
    if (command.isActive() != null) {
      template.setIsActive(command.isActive());
    }

    PageTemplate saved = pageTemplateRepository.save(template);
    log.info("Updated page template: {} ({})", saved.getName(), saved.getUid());

    return pageTemplateMapper.toDto(saved);
  }

  @Override
  public void delete(Long id) {
    PageTemplate template = pageTemplateRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("PageTemplate", id));

    if (Boolean.TRUE.equals(template.getIsSystem())) {
      throw new IllegalArgumentException("Cannot delete system template: " + template.getName());
    }

    pageTemplateRepository.deleteById(id);
    log.info("Deleted page template: {} ({})", template.getName(), template.getUid());
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
    log.info("Added slot '{}' to template '{}'", command.slotName(), template.getName());

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
        template.getName(), pageId, newSlots.size());
  }
}
