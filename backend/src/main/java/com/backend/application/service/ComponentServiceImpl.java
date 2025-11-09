package com.backend.application.service;

import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.repository.ComponentI18nRepository;
import com.backend.domain.repository.ComponentRepository;
import com.backend.infrastructure.util.UuidUidGenerator;
import com.backend.presentation.dto.request.ComponentCreateRequest;
import com.backend.presentation.dto.response.ComponentDetailResponse;
import com.backend.presentation.dto.response.ComponentListResponse;
import com.backend.presentation.dto.response.ComponentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComponentServiceImpl implements ComponentService {

    private final ComponentRepository componentRepository;
    private final ComponentI18nRepository componentI18nRepository;
    private final ComponentTypeService componentTypeService;

    @Override
    @Transactional
    public ComponentResponse createComponent(ComponentCreateRequest request, Long userId) {
        componentTypeService.getComponentTypeById(request.componentTypeId());

        Component component = new Component();
        component.setUuid(UuidUidGenerator.generateUuid());
        component.setUid(generateUniqueUid());
        component.setComponentTypeId(request.componentTypeId());
        component.setCode(request.code());
        component.setName(request.name());
        component.setBaseData(request.baseData());
        component.setExtendedData(request.extendedData());
        component.setStatus(request.status() != null ? request.status() : ComponentStatus.DRAFT);
        component.setCreatedAt(LocalDateTime.now());
        component.setUpdatedAt(LocalDateTime.now());
        component.setCreatedBy(userId);
        component.setUpdatedBy(userId);

        component = componentRepository.save(component);
        return ComponentResponse.from(component);
    }

    @Override
    @Transactional(readOnly = true)
    public ComponentResponse getComponentById(Long id) {
        Component component = componentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Component not found with id: " + id));
        return ComponentResponse.from(component);
    }

    @Override
    @Transactional(readOnly = true)
    public ComponentDetailResponse getComponentWithI18n(Long id) {
        Component component = componentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Component not found with id: " + id));
        List<ComponentI18n> i18nList = componentI18nRepository.findByComponentId(id);
        return ComponentDetailResponse.from(component, i18nList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComponentResponse> getAllComponents() {
        return componentRepository.findAll()
                .stream()
                .map(ComponentResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComponentListResponse> getAllComponentsWithTranslations() {
        List<Component> components = componentRepository.findAll();
        List<ComponentI18n> allTranslations = componentI18nRepository.findAll();
        Map<Long, List<ComponentI18n>> translationsByComponent = allTranslations.stream()
                .collect(Collectors.groupingBy(ComponentI18n::getComponentId));
        return components.stream()
                .map(component -> ComponentListResponse.from(
                        component,
                        translationsByComponent.getOrDefault(component.getId(), List.of())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComponentResponse> getComponentsByTypeId(Long typeId) {
        return componentRepository.findByComponentTypeId(typeId)
                .stream()
                .map(ComponentResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ComponentResponse updateComponent(Long id, ComponentCreateRequest request, Long userId) {
        Component component = componentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Component not found with id: " + id));

        componentTypeService.getComponentTypeById(request.componentTypeId());

        component.setComponentTypeId(request.componentTypeId());
        component.setCode(request.code());
        component.setName(request.name());
        component.setBaseData(request.baseData());
        component.setExtendedData(request.extendedData());
        if (request.status() != null) {
            component.setStatus(request.status());
        }
        component.setUpdatedAt(LocalDateTime.now());
        component.setUpdatedBy(userId);

        component = componentRepository.save(component);
        return ComponentResponse.from(component);
    }

    @Override
    @Transactional
    public void deleteComponent(Long id) {
        Component component = componentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Component not found with id: " + id));
        componentRepository.delete(component);
    }

    private String generateUniqueUid() {
        String uid;
        do {
            uid = UuidUidGenerator.generateUid();
        } while (componentRepository.existsByUid(uid));
        return uid;
    }
}
