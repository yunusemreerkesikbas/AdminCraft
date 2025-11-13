package com.backend.application.service;

import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.repository.ComponentEntryI18nRepository;
import com.backend.domain.repository.ComponentEntryRepository;
import com.backend.domain.repository.ComponentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComponentEntryServiceImpl implements ComponentEntryService {

    private final ComponentEntryRepository entryRepository;
    private final ComponentEntryI18nRepository entryI18nRepository;
    private final ComponentRepository componentRepository;

    @Override
    @Transactional
    public ComponentEntry createEntry(ComponentEntry entry) {
        componentRepository.findById(entry.getComponentId())
                .orElseThrow(() -> new IllegalArgumentException("Component not found: " + entry.getComponentId()));

        ComponentEntry saved = entryRepository.save(entry);
        log.info("Created entry {} for component {}", saved.getId(), saved.getComponentId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public ComponentEntry getEntryById(Long id) {
        return entryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entry not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComponentEntry> getEntriesByComponentId(Long componentId) {
        return entryRepository.findByComponentIdOrderBySortOrder(componentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<ComponentEntry, List<ComponentEntryI18n>> getEntriesWithI18n(Long componentId) {
        List<ComponentEntry> entries = entryRepository.findByComponentIdOrderBySortOrder(componentId);
        List<Long> entryIds = entries.stream().map(ComponentEntry::getId).collect(Collectors.toList());

        Map<Long, List<ComponentEntryI18n>> i18nByEntry = new HashMap<>();
        for (Long entryId : entryIds) {
            i18nByEntry.put(entryId, entryI18nRepository.findByEntryId(entryId));
        }

        Map<ComponentEntry, List<ComponentEntryI18n>> result = new HashMap<>();
        for (ComponentEntry entry : entries) {
            result.put(entry, i18nByEntry.getOrDefault(entry.getId(), List.of()));
        }

        return result;
    }

    @Override
    @Transactional
    public ComponentEntry updateEntry(ComponentEntry entry) {
        ComponentEntry existing = entryRepository.findById(entry.getId())
                .orElseThrow(() -> new IllegalArgumentException("Entry not found: " + entry.getId()));

        existing.setSortOrder(entry.getSortOrder());
        existing.setIsVisible(entry.getIsVisible());
        existing.setStyleClasses(entry.getStyleClasses());
        existing.setStatus(entry.getStatus());
        existing.setUpdatedBy(entry.getUpdatedBy());

        return entryRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteEntry(Long id) {
        ComponentEntry entry = entryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entry not found: " + id));
        entryRepository.delete(entry);
        log.info("Deleted entry {}", id);
    }
}



