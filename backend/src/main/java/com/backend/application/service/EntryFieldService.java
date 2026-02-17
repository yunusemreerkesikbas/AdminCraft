package com.backend.application.service;

import com.backend.application.dto.request.CreateEntryFieldRequest;
import com.backend.application.query.GetEntryFieldsByTypeQuery;
import com.backend.presentation.dto.response.EntryFieldDefinitionResponse;
import java.util.List;

public interface EntryFieldService {
    EntryFieldDefinitionResponse addField(Long componentTypeId, CreateEntryFieldRequest request);
    List<EntryFieldDefinitionResponse> getFieldsByType(GetEntryFieldsByTypeQuery query);
    EntryFieldDefinitionResponse getFieldById(Long id);
}


