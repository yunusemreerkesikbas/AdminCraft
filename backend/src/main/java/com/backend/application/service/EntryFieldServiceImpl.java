package com.backend.application.service;

import com.backend.application.dto.request.CreateEntryFieldRequest;
import com.backend.application.dto.response.EntryFieldDefinitionResult;
import com.backend.application.mapper.EntryFieldMapper;
import com.backend.application.query.GetEntryFieldsByTypeQuery;
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
    private final EntryFieldMapper mapper;

    @Override
    @Transactional
    public EntryFieldDefinitionResult addField(Long componentTypeId, CreateEntryFieldRequest request) {
        componentTypeRepository.findById(componentTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Component type not found: " + componentTypeId));

        if (fieldRepository.existsByComponentTypeIdAndFieldKey(componentTypeId, request.fieldKey())) {
            throw new IllegalArgumentException("Field already exists: " + request.fieldKey());
        }

        EntryFieldDefinition field = mapper.toEntity(componentTypeId, request);
        EntryFieldDefinition saved = fieldRepository.save(field);
        log.info("Added field {} to component type {}", saved.getFieldKey(), saved.getComponentTypeId());
        return mapper.toResult(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntryFieldDefinitionResult> getFieldsByType(GetEntryFieldsByTypeQuery query) {
        List<EntryFieldDefinition> fields = fieldRepository.findByComponentTypeId(query.componentTypeId());
        return mapper.toResultList(fields);
    }

    @Override
    @Transactional(readOnly = true)
    public EntryFieldDefinitionResult getFieldById(Long id) {
        EntryFieldDefinition field = fieldRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Field definition not found: " + id));
        return mapper.toResult(field);
    }
}
