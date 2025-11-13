package com.backend.application.mapper;

import com.backend.application.command.CreateEntryFieldCommand;
import com.backend.domain.entity.EntryFieldDefinition;
import com.backend.presentation.dto.response.EntryFieldDefinitionResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntryFieldMapper {

  public EntryFieldDefinition toEntity(CreateEntryFieldCommand command) {
    EntryFieldDefinition entity = new EntryFieldDefinition();
    entity.setComponentTypeId(command.componentTypeId());
    entity.setFieldKey(command.fieldKey());
    entity.setFieldType(command.fieldType());
    entity.setIsRequired(command.isRequired() != null ? command.isRequired() : false);
    entity.setMaxLength(command.maxLength());
    entity.setMinValue(command.minValue());
    entity.setMaxValue(command.maxValue());
    return entity;
  }

  public EntryFieldDefinitionResponse toResponse(EntryFieldDefinition entity) {
    return EntryFieldDefinitionResponse.from(entity);
  }

  public List<EntryFieldDefinitionResponse> toResponseList(List<EntryFieldDefinition> entities) {
    return entities.stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }
}
