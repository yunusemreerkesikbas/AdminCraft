package com.backend.application.service;

import com.backend.domain.entity.EntryFieldDefinition;
import java.util.List;

public interface EntryFieldService {
    EntryFieldDefinition addField(Long componentTypeId, EntryFieldDefinition field);
    List<EntryFieldDefinition> getFieldsByTypeId(Long componentTypeId);
    EntryFieldDefinition getFieldById(Long id);
}



