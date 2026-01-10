package com.backend.application.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.command.ComponentTypeCommands.CreateComponentTypeCommand;
import com.backend.application.command.ComponentTypeCommands.DeleteComponentTypeCommand;
import com.backend.application.command.ComponentTypeCommands.UpdateComponentTypeCommand;
import com.backend.application.query.ComponentTypeQueries.GetAllComponentTypesQuery;
import com.backend.application.query.ComponentTypeQueries.GetComponentTypeByIdQuery;
import com.backend.application.query.ComponentTypeQueries.GetComponentTypesByCategoryQuery;
import com.backend.domain.entity.ComponentType;
import com.backend.domain.repository.ComponentTypeRepository;
import com.backend.infrastructure.util.UuidUidGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComponentTypeServiceImpl implements ComponentTypeService {

    private final ComponentTypeRepository componentTypeRepository;

    @Override
    @Transactional
    public ComponentType createComponentType(CreateComponentTypeCommand command) {
        log.debug("Creating component type with name: {}", command.name());

        ComponentType componentType = new ComponentType();
        componentType.setUid(generateUniqueUid());
        componentType.setName(command.name());
        componentType.setCategory(command.category());
        componentType.setCreatedBy(command.userId());
        componentType.setUpdatedBy(command.userId());

        componentType = componentTypeRepository.save(componentType);
        log.info("Component type created successfully with id: {}", componentType.getId());
        return componentType;
    }

    @Override
    @Transactional(readOnly = true)
    public ComponentType getComponentTypeById(GetComponentTypeByIdQuery query) {
        return componentTypeRepository.findById(query.id())
                .orElseThrow(() -> new IllegalArgumentException("ComponentType not found with id: " + query.id()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComponentType> getAllComponentTypes(GetAllComponentTypesQuery query) {
        return componentTypeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComponentType> searchComponentTypes(Pageable pageable, String searchQuery) {
        if (searchQuery == null || searchQuery.trim().length() < 2) {
            return componentTypeRepository.findAllPaged(pageable);
        }
        return componentTypeRepository.searchPaged(searchQuery.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComponentType> getComponentTypesByCategory(GetComponentTypesByCategoryQuery query) {
        return componentTypeRepository.findByCategory(query.category());
    }

    @Override
    @Transactional
    public ComponentType updateComponentType(UpdateComponentTypeCommand command) {
        log.debug("Updating component type with id: {}", command.id());

        ComponentType componentType = componentTypeRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("ComponentType not found with id: " + command.id()));

        componentType.setName(command.name());
        componentType.setCategory(command.category());
        componentType.setUpdatedBy(command.userId());

        componentType = componentTypeRepository.save(componentType);
        log.info("Component type updated successfully with id: {}", command.id());
        return componentType;
    }

    @Override
    @Transactional
    public void deleteComponentType(DeleteComponentTypeCommand command) {
        ComponentType componentType = componentTypeRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("ComponentType not found with id: " + command.id()));

        componentTypeRepository.delete(componentType);
    }

    private String generateUniqueUid() {
        String uid;
        do {
            uid = UuidUidGenerator.generateUid();
        } while (componentTypeRepository.existsByUid(uid));
        return uid;
    }
}
