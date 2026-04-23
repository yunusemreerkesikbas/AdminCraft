package com.backend.application.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.application.command.PageTemplateCommands.CreatePageTemplateCommand;
import com.backend.application.command.PageTemplateCommands.CreateTemplateSlotCommand;
import com.backend.application.command.PageTemplateCommands.ReorderTemplateSlotsCommand;
import com.backend.application.command.PageTemplateCommands.UpdatePageTemplateCommand;
import com.backend.application.dto.response.BulkDeleteResultResponse;
import com.backend.application.dto.template.PageTemplateDto;
import com.backend.application.dto.template.TemplateSlotDto;

public interface PageTemplateService {

  List<PageTemplateDto> getAll();

  Page<PageTemplateDto> getTemplates(Pageable pageable, String search);

  List<PageTemplateDto> getActiveTemplates();

  PageTemplateDto getById(Long id);

  PageTemplateDto create(CreatePageTemplateCommand command);

  PageTemplateDto update(Long id, UpdatePageTemplateCommand command);

  void delete(Long id);

  BulkDeleteResultResponse bulkDeletePageTemplates(List<Long> ids);

  TemplateSlotDto addSlot(Long templateId, CreateTemplateSlotCommand command);

  void removeSlot(Long templateId, String slotName);

  void reorderSlots(Long templateId, ReorderTemplateSlotsCommand command);

  void assignTemplateToPage(Long pageId, Long templateId);
}
