package com.backend.application.service;

import com.backend.application.command.CreateEntryFieldCommand;
import com.backend.application.mapper.EntryFieldMapper;
import com.backend.application.query.GetEntryFieldsByTypeQuery;
import com.backend.domain.entity.EntryFieldDefinition;
import com.backend.domain.repository.ComponentTypeRepository;
import com.backend.domain.repository.EntryFieldDefinitionRepository;
import com.backend.presentation.dto.response.EntryFieldDefinitionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntryFieldServiceImpl implements EntryFieldService {

    private static final int MAX_FIELDS_PER_TYPE = 10;

    private final EntryFieldDefinitionRepository fieldRepository;
    private final ComponentTypeRepository componentTypeRepository;
    private final EntryFieldValidator fieldValidator;
    private final RuntimeMigrationService migrationService;
    private final EntryFieldMapper mapper;

    @Override
    @Transactional
    public EntryFieldDefinitionResponse addField(CreateEntryFieldCommand command) {
        componentTypeRepository.findById(command.componentTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Component type not found: " + command.componentTypeId()));

        EntryFieldDefinition field = mapper.toEntity(command);

        long existingFieldCount = fieldRepository.findByComponentTypeId(command.componentTypeId()).size();
        
        if (existingFieldCount >= MAX_FIELDS_PER_TYPE) {
            throw new com.backend.domain.exception.MaxFieldLimitException(
                MAX_FIELDS_PER_TYPE, command.componentTypeId()
            );
        }
        
        fieldValidator.validate(field, existingFieldCount);

        if (fieldRepository.existsByComponentTypeIdAndFieldKey(command.componentTypeId(), command.fieldKey())) {
            throw new IllegalArgumentException("Field already exists: " + command.fieldKey());
        }

        EntryFieldDefinition saved = fieldRepository.save(field);

        String migrationVersion = generateMigrationVersion(command.componentTypeId(), saved.getId());
        saved.setMigrationVersion(migrationVersion);
        
        migrationService.addFieldColumn(saved);
        
        saved.setAppliedAt(java.time.LocalDateTime.now());
        fieldRepository.save(saved);

        log.info("Added field {} to component type {} with migration version {}", 
            saved.getFieldKey(), saved.getComponentTypeId(), migrationVersion);
        return mapper.toResponse(saved);
    }
    
    protected String generateMigrationVersion(Long componentTypeId, Long fieldId) {
        return String.format("V%d_%s_%03d", 
            componentTypeId, 
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")),
            fieldId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntryFieldDefinitionResponse> getFieldsByType(GetEntryFieldsByTypeQuery query) {
        List<EntryFieldDefinition> fields = fieldRepository.findByComponentTypeId(query.componentTypeId());
        return mapper.toResponseList(fields);
    }

    @Override
    @Transactional(readOnly = true)
    public EntryFieldDefinitionResponse getFieldById(Long id) {
        EntryFieldDefinition field = fieldRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Field definition not found: " + id));
        return mapper.toResponse(field);
    }
}

