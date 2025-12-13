package com.backend.application.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.backend.application.codec.TemplateAllowedTypesCodec;
import com.backend.application.dto.template.PageTemplateDto;
import com.backend.application.dto.template.TemplateSlotDto;
import com.backend.domain.entity.PageTemplate;
import com.backend.domain.entity.TemplateSlot;

@Component
public class PageTemplateMapper {

  private final TemplateAllowedTypesCodec allowedTypesCodec;

  public PageTemplateMapper(TemplateAllowedTypesCodec allowedTypesCodec) {
    this.allowedTypesCodec = allowedTypesCodec;
  }

  public String encodeAllowedTypes(List<String> allowedTypes) {
    return allowedTypesCodec.encode(allowedTypes);
  }

  public PageTemplateDto toDto(PageTemplate entity) {
    return PageTemplateDto.builder()
        .id(entity.getId())
        .uuid(entity.getUuid())
        .uid(entity.getUid())
        .name(entity.getName())
        .description(entity.getDescription())
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
}
