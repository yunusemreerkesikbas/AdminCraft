package com.backend.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.command.ComponentTypeCommands.CreateComponentTypeCommand;
import com.backend.application.command.ComponentTypeCommands.DeleteComponentTypeCommand;
import com.backend.application.command.ComponentTypeCommands.UpdateComponentTypeCommand;
import com.backend.application.query.ComponentTypeQueries.GetAllComponentTypesQuery;
import com.backend.application.query.ComponentTypeQueries.GetComponentTypeByCodeQuery;
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
        log.debug("Creating component type with code: {}", command.code());

        ComponentType componentType = new ComponentType();
        componentType.setUid(generateUniqueUid());
        componentType.setCode(command.code());
        componentType.setName(command.name());
        componentType.setCategory(command.category());
        componentType.setIcon(command.icon());
        componentType.setIsSystem(false);
        componentType.setCreatedBy(command.userId());
        componentType.setUpdatedBy(command.userId());

        componentType = componentTypeRepository.save(componentType);
        log.info("Component type created successfully with id: {} and code: {}", componentType.getId(),
                componentType.getCode());
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
    public ComponentType getComponentTypeByCode(GetComponentTypeByCodeQuery query) {
        return componentTypeRepository.findByCode(query.code())
                .orElseThrow(() -> new IllegalArgumentException("ComponentType not found with code: " + query.code()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComponentType> getAllComponentTypes(GetAllComponentTypesQuery query) {
        return componentTypeRepository.findAll();
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

        if (componentType.getIsSystem()) {
            throw new IllegalStateException("Cannot update system component type");
        }

        componentType.setCode(command.code());
        componentType.setName(command.name());
        componentType.setCategory(command.category());
        componentType.setIcon(command.icon());
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

        if (componentType.getIsSystem()) {
            throw new IllegalStateException("Cannot delete system component type");
        }

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
