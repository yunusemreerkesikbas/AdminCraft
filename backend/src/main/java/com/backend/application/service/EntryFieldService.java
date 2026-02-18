package com.backend.application.service;

import com.backend.application.dto.request.CreateEntryFieldRequest;
import com.backend.application.dto.response.EntryFieldDefinitionResult;
import com.backend.application.query.GetEntryFieldsByTypeQuery;

import java.util.List;

public interface EntryFieldService {
    EntryFieldDefinitionResult addField(Long componentTypeId, CreateEntryFieldRequest request);
    List<EntryFieldDefinitionResult> getFieldsByType(GetEntryFieldsByTypeQuery query);
    EntryFieldDefinitionResult getFieldById(Long id);
}
