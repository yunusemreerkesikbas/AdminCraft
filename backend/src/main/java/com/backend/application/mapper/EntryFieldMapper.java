package com.backend.application.mapper;

import com.backend.application.dto.request.CreateEntryFieldRequest;
import com.backend.application.dto.response.EntryFieldDefinitionResult;
import com.backend.domain.entity.EntryFieldDefinition;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EntryFieldMapper {

  public EntryFieldDefinition toEntity(Long componentTypeId, CreateEntryFieldRequest request) {
    EntryFieldDefinition entity = new EntryFieldDefinition();
    entity.setComponentTypeId(componentTypeId);
    entity.setFieldKey(request.fieldKey());
    entity.setFieldType(request.fieldType());
    return entity;
  }

  public EntryFieldDefinitionResult toResult(EntryFieldDefinition entity) {
    return EntryFieldDefinitionResult.from(entity);
  }

  public List<EntryFieldDefinitionResult> toResultList(List<EntryFieldDefinition> entities) {
    return entities.stream()
        .map(this::toResult)
        .toList();
  }
}
