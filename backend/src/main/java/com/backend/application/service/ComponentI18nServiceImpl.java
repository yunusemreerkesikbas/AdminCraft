package com.backend.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.command.ComponentI18nCommands.DeleteComponentI18nCommand;
import com.backend.application.command.ComponentI18nCommands.PublishComponentI18nCommand;
import com.backend.application.command.ComponentI18nCommands.UnpublishComponentI18nCommand;
import com.backend.application.command.ComponentI18nCommands.UpsertComponentI18nCommand;
import com.backend.application.query.ComponentI18nQueries.GetComponentI18nByComponentIdQuery;
import com.backend.application.query.ComponentI18nQueries.GetComponentI18nQuery;
import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.repository.ComponentI18nRepository;
import com.backend.domain.util.UuidUidGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComponentI18nServiceImpl implements ComponentI18nService {

    private final ComponentI18nRepository componentI18nRepository;

    @Override
    @Transactional
    public ComponentI18n upsertComponentI18n(UpsertComponentI18nCommand command) {
        ComponentI18n componentI18n = componentI18nRepository
                .findByComponentIdAndLanguage(command.componentId(), command.language())
                .orElse(null);

        if (componentI18n == null) {
            componentI18n = new ComponentI18n();
            componentI18n.setUid(generateUniqueUid());
            componentI18n.setComponentId(command.componentId());
            componentI18n.setLanguage(command.language());
        }

        componentI18n.setTitle(command.title());
        componentI18n.setSubtitle(command.subtitle());
        componentI18n.setDescription(command.description());
        if (command.status() != null) {
            componentI18n.setStatus(command.status());
        }

        return componentI18nRepository.save(componentI18n);
    }

    @Override
    @Transactional(readOnly = true)
    public ComponentI18n getComponentI18n(GetComponentI18nQuery query) {
        return componentI18nRepository
                .findByComponentIdAndLanguage(query.componentId(), query.language())
                .orElseThrow(() -> new IllegalArgumentException(
                        "ComponentI18n not found for componentId: " + query.componentId() + " and language: "
                                + query.language()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComponentI18n> getComponentI18nByComponentId(GetComponentI18nByComponentIdQuery query) {
        return componentI18nRepository.findByComponentId(query.componentId());
    }

    @Override
    @Transactional
    public ComponentI18n publishComponentI18n(PublishComponentI18nCommand command) {
        ComponentI18n componentI18n = componentI18nRepository
                .findByComponentIdAndLanguage(command.componentId(), command.language())
                .orElseThrow(() -> new IllegalArgumentException(
                        "ComponentI18n not found for componentId: " + command.componentId() + " and language: "
                                + command.language()));

        componentI18n.setStatus(com.backend.domain.enums.ComponentStatus.PUBLISHED);
        return componentI18nRepository.save(componentI18n);
    }

    @Override
    @Transactional
    public ComponentI18n unpublishComponentI18n(UnpublishComponentI18nCommand command) {
        ComponentI18n componentI18n = componentI18nRepository
                .findByComponentIdAndLanguage(command.componentId(), command.language())
                .orElseThrow(() -> new IllegalArgumentException(
                        "ComponentI18n not found for componentId: " + command.componentId() + " and language: "
                                + command.language()));

        componentI18n.setStatus(com.backend.domain.enums.ComponentStatus.DRAFT);
        return componentI18nRepository.save(componentI18n);
    }

    @Override
    @Transactional
    public void deleteComponentI18n(DeleteComponentI18nCommand command) {
        ComponentI18n componentI18n = componentI18nRepository
                .findByComponentIdAndLanguage(command.componentId(), command.language())
                .orElseThrow(() -> new IllegalArgumentException(
                        "ComponentI18n not found for componentId: " + command.componentId() + " and language: "
                                + command.language()));
        componentI18nRepository.delete(componentI18n);
    }

    private String generateUniqueUid() {
        String uid;
        do {
            uid = UuidUidGenerator.generateUid();
        } while (componentI18nRepository.existsByUid(uid));
        return uid;
    }
}
