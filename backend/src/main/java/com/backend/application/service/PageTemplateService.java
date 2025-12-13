package com.backend.application.service;

import java.util.List;

import com.backend.application.command.PageTemplateCommands.CreatePageTemplateCommand;
import com.backend.application.command.PageTemplateCommands.CreateTemplateSlotCommand;
import com.backend.application.command.PageTemplateCommands.UpdatePageTemplateCommand;
import com.backend.application.dto.template.PageTemplateDto;
import com.backend.application.dto.template.TemplateSlotDto;

public interface PageTemplateService {

  List<PageTemplateDto> getAll();

  List<PageTemplateDto> getActiveTemplates();

  PageTemplateDto getById(Long id);

  PageTemplateDto create(CreatePageTemplateCommand command);

  PageTemplateDto update(Long id, UpdatePageTemplateCommand command);

  void delete(Long id);

  TemplateSlotDto addSlot(Long templateId, CreateTemplateSlotCommand command);

  void removeSlot(Long templateId, String slotName);

  void assignTemplateToPage(Long pageId, Long templateId);
}
