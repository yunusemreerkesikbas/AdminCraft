package com.backend.application.service;

import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.enums.Language;
import java.util.List;
import java.util.Map;

public interface ComponentEntryI18nService {
    ComponentEntryI18n upsertEntryI18n(Long entryId, Language language, Map<String, Object> data);
    ComponentEntryI18n getEntryI18n(Long entryId, Language language);
    List<ComponentEntryI18n> getEntryI18nByEntryId(Long entryId);
    ComponentEntryI18n publishEntryI18n(Long entryId, Language language);
    void deleteEntryI18n(Long entryId, Language language);
}



