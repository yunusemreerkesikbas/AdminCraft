package com.backend.application.service;

import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.entity.ComponentEntryI18n;
import java.util.List;
import java.util.Map;

public interface ComponentEntryService {
    ComponentEntry createEntry(ComponentEntry entry);
    ComponentEntry getEntryById(Long id);
    List<ComponentEntry> getEntriesByComponentId(Long componentId);
    Map<ComponentEntry, List<ComponentEntryI18n>> getEntriesWithI18n(Long componentId);
    ComponentEntry updateEntry(ComponentEntry entry);
    void deleteEntry(Long id);
}



