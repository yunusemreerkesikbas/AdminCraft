package com.backend.application.service;

import java.util.List;
import java.util.Map;

import com.backend.application.dto.request.CreateComponentEntryCompositeRequest;
import com.backend.application.dto.request.UpdateComponentEntryCompositeRequest;
import com.backend.application.dto.response.ComponentEntryCompositeResponse;
import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.entity.ComponentEntryI18n;

public interface ComponentEntryService {
    ComponentEntry createEntry(ComponentEntry entry);

    ComponentEntry getEntryById(Long id);

    List<ComponentEntry> getEntriesByComponentId(Long componentId);

    Map<ComponentEntry, List<ComponentEntryI18n>> getEntriesWithI18n(Long componentId);

    ComponentEntry updateEntry(ComponentEntry entry);

    void deleteEntry(Long id);

    ComponentEntryCompositeResponse createComposite(CreateComponentEntryCompositeRequest request);

    ComponentEntryCompositeResponse updateComposite(Long id, UpdateComponentEntryCompositeRequest request);

    ComponentEntryCompositeResponse getEntryWithTranslations(Long id);
}
