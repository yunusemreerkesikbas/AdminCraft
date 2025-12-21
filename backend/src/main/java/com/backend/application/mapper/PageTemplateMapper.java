package com.backend.application.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.backend.application.codec.TemplateAllowedTypesCodec;
import com.backend.application.dto.template.PageTemplateDto;
import com.backend.application.dto.template.TemplateSlotDto;
import com.backend.domain.entity.PageTemplate;
import com.backend.domain.entity.PageTemplateI18n;
import com.backend.domain.entity.TemplateSlot;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.PageTemplateI18nRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PageTemplateMapper {

  private static final Language DEFAULT_LANGUAGE = Language.TR;

  private final TemplateAllowedTypesCodec allowedTypesCodec;
  private final PageTemplateI18nRepository templateI18nRepository;

  public String encodeAllowedTypes(List<String> allowedTypes) {
    return allowedTypesCodec.encode(allowedTypes);
  }

  public PageTemplateDto toDto(PageTemplate entity) {
    PageTemplateI18n i18n = templateI18nRepository.findByTemplateIdAndLanguage(entity.getId(), getDefaultLanguage())
        .orElse(null);

    String name = i18n != null ? i18n.getName() : null;
    String description = i18n != null ? i18n.getDescription() : null;

    return PageTemplateDto.builder()
        .id(entity.getId())
        .uuid(entity.getUuid())
        .uid(entity.getUid())
        .name(name)
        .description(description)
        .isSystem(entity.getIsSystem())
        .isActive(entity.getIsActive())
        .slots(entity.getSlots() != null
            ? entity.getSlots().stream().map(this::toSlotDto).toList()
            : List.of())
        .build();
  }

  public PageTemplateDto toDto(PageTemplate entity, String name, String description) {
    return PageTemplateDto.builder()
        .id(entity.getId())
        .uuid(entity.getUuid())
        .uid(entity.getUid())
        .name(name)
        .description(description)
        .isSystem(entity.getIsSystem())
        .isActive(entity.getIsActive())
        .slots(entity.getSlots() != null
            ? entity.getSlots().stream().map(this::toSlotDto).toList()
            : List.of())
        .build();
  }

  public TemplateSlotDto toSlotDto(TemplateSlot entity) {
    return TemplateSlotDto.builder()
        .id(entity.getId())
        .uuid(entity.getUuid())
        .slotName(entity.getSlotName())
        .position(entity.getPosition())
        .sortOrder(entity.getSortOrder())
        .isRequired(entity.getIsRequired())
        .maxComponents(entity.getMaxComponents())
        .allowedTypes(allowedTypesCodec.decode(entity.getAllowedTypes()))
        .build();
  }

  private Language getDefaultLanguage() {
    // TODO: In future, can be fetched from tenant settings
    return DEFAULT_LANGUAGE;
  }
}
