package com.backend.application.mapper;

import com.backend.application.dto.request.CreateEntryFieldRequest;
import com.backend.domain.entity.EntryFieldDefinition;
import com.backend.presentation.dto.response.EntryFieldDefinitionResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntryFieldMapper {

  public EntryFieldDefinition toEntity(Long componentTypeId, CreateEntryFieldRequest request) {
    EntryFieldDefinition entity = new EntryFieldDefinition();
    entity.setComponentTypeId(componentTypeId);
    entity.setFieldKey(request.fieldKey());
    entity.setFieldType(request.fieldType());
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
