package com.backend.application.service;

import com.backend.application.command.ComponentI18nCommands.*;
import com.backend.application.query.ComponentI18nQueries.*;
import com.backend.domain.entity.ComponentI18n;
import java.util.List;

public interface ComponentI18nService {
    ComponentI18n upsertComponentI18n(UpsertComponentI18nCommand command);
    ComponentI18n getComponentI18n(GetComponentI18nQuery query);
    List<ComponentI18n> getComponentI18nByComponentId(GetComponentI18nByComponentIdQuery query);
    ComponentI18n publishComponentI18n(PublishComponentI18nCommand command);
    ComponentI18n unpublishComponentI18n(UnpublishComponentI18nCommand command);
    void deleteComponentI18n(DeleteComponentI18nCommand command);
}
