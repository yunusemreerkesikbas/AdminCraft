package com.backend.application.service;

import com.backend.domain.entity.EntryFieldDefinition;
import com.backend.domain.repository.ComponentTypeRepository;
import com.backend.domain.repository.EntryFieldDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntryFieldServiceImpl implements EntryFieldService {

    private final EntryFieldDefinitionRepository fieldRepository;
    private final ComponentTypeRepository componentTypeRepository;
    private final EntryFieldValidator fieldValidator;
    private final RuntimeMigrationService migrationService;

    @Override
    @Transactional
    public EntryFieldDefinition addField(Long componentTypeId, EntryFieldDefinition field) {
        componentTypeRepository.findById(componentTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Component type not found: " + componentTypeId));

        long existingFieldCount = fieldRepository.findByComponentTypeId(componentTypeId).size();
        fieldValidator.validate(field, existingFieldCount);
        fieldValidator.validateLabels(field.getLabelTr(), field.getLabelEn());

        if (fieldRepository.existsByComponentTypeIdAndFieldKey(componentTypeId, field.getFieldKey())) {
            throw new IllegalArgumentException("Field already exists: " + field.getFieldKey());
        }

        field.setComponentTypeId(componentTypeId);
        EntryFieldDefinition saved = fieldRepository.save(field);

        migrationService.addFieldColumn(saved);

        log.info("Added field {} to component type {}", field.getFieldKey(), componentTypeId);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntryFieldDefinition> getFieldsByTypeId(Long componentTypeId) {
        return fieldRepository.findByComponentTypeId(componentTypeId);
    }

    @Override
    @Transactional(readOnly = true)
    public EntryFieldDefinition getFieldById(Long id) {
        return fieldRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Field definition not found: " + id));
    }
}

