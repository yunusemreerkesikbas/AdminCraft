package com.backend.application.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.request.CreateProductVariantOptionRequest;
import com.backend.application.dto.request.ProductVariantOptionValueRequest;
import com.backend.application.dto.request.UpdateProductVariantOptionRequest;
import com.backend.application.dto.response.ProductVariantOptionResponse;
import com.backend.domain.entity.ProductVariantOption;
import com.backend.domain.entity.ProductVariantOptionValue;
import com.backend.domain.exception.BusinessRuleViolationException;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.infrastructure.persistence.repository.ProductVariantOptionRepository;
import com.backend.infrastructure.persistence.repository.ProductVariantRepository;
import com.backend.infrastructure.tenant.TenantContext;
import com.backend.shared.util.SlugGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductVariantOptionServiceImpl implements ProductVariantOptionService {

    private final ProductVariantOptionRepository optionRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantOptionResponse> findAll() {
        TenantContext.validateActive();
        return optionRepository.findAllByOrderBySortOrderAscIdAsc()
                .stream()
                .map(ProductVariantOptionResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductVariantOptionResponse findById(Long id) {
        TenantContext.validateActive();
        ProductVariantOption option = optionRepository.findByIdWithValues(id)
                .orElseThrow(() -> new EntityNotFoundException("ProductVariantOption", id));
        return ProductVariantOptionResponse.from(option);
    }

    @Override
    @Transactional
    public ProductVariantOptionResponse create(CreateProductVariantOptionRequest request) {
        TenantContext.validateActive();
        ProductVariantOption option = new ProductVariantOption();
        option.setCode(generateOptionCode(request.name()));
        option.setName(request.name());
        option.setDisplayType(request.displayType());
        option.setSortOrder(request.sortOrder());
        option.setActive(request.active());
        replaceValues(option, request.values());
        ProductVariantOption saved = optionRepository.save(option);
        log.info("Created product variant option: {} ({})", saved.getCode(), saved.getId());
        return ProductVariantOptionResponse.from(saved);
    }

    @Override
    @Transactional
    public ProductVariantOptionResponse update(Long id, UpdateProductVariantOptionRequest request) {
        TenantContext.validateActive();
        ProductVariantOption option = optionRepository.findByIdWithValues(id)
                .orElseThrow(() -> new EntityNotFoundException("ProductVariantOption", id));
        if (request.name() != null && !request.name().isBlank()) {
            option.setName(request.name());
        }
        if (request.displayType() != null) {
            option.setDisplayType(request.displayType());
        }
        if (request.sortOrder() != null) {
            option.setSortOrder(request.sortOrder());
        }
        if (request.active() != null) {
            option.setActive(request.active());
        }
        if (request.values() != null) {
            syncValues(option, request.values());
        }
        ProductVariantOption saved = optionRepository.save(option);
        log.info("Updated product variant option: {} ({})", saved.getCode(), saved.getId());
        return ProductVariantOptionResponse.from(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        TenantContext.validateActive();
        ProductVariantOption option = optionRepository.findByIdWithValues(id)
                .orElseThrow(() -> new EntityNotFoundException("ProductVariantOption", id));
        if (productVariantRepository.existsByOptionId(id)) {
            throw new BusinessRuleViolationException("product.variant.option.in.use");
        }
        optionRepository.delete(option);
        log.info("Deleted product variant option: {}", id);
    }

    private void syncValues(ProductVariantOption option, List<ProductVariantOptionValueRequest> values) {
        Map<Long, ProductVariantOptionValue> existingValues = option.getValues().stream()
                .filter(value -> value.getId() != null)
                .collect(Collectors.toMap(ProductVariantOptionValue::getId, Function.identity()));
        Set<Long> requestedIds = values.stream()
                .map(ProductVariantOptionValueRequest::id)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (!existingValues.keySet().containsAll(requestedIds)) {
            throw new IllegalArgumentException("Variant option value does not belong to this option");
        }

        Set<Long> removedIds = existingValues.keySet().stream()
                .filter(id -> !requestedIds.contains(id))
                .collect(Collectors.toSet());
        if (!removedIds.isEmpty()) {
            Set<Long> usedRemovedIds = productVariantRepository.findUsedOptionValueIds(removedIds).stream()
                    .collect(Collectors.toSet());
            if (!usedRemovedIds.isEmpty()) {
                throw new BusinessRuleViolationException("product.variant.option.value.in.use");
            }
            option.getValues().removeIf(value -> removedIds.contains(value.getId()));
        }

        int fallbackSortOrder = 0;
        for (ProductVariantOptionValueRequest request : values) {
            ProductVariantOptionValue value = request.id() == null
                    ? new ProductVariantOptionValue()
                    : existingValues.get(request.id());
            if (request.id() == null) {
                value.setCode(generateValueCode(option, request.label()));
                option.addValue(value);
            }
            value.setLabel(request.label());
            value.setSwatchValue(request.swatchValue());
            value.setSortOrder(request.sortOrder() != null ? request.sortOrder() : fallbackSortOrder);
            value.setActive(request.active() != null ? request.active() : true);
            fallbackSortOrder++;
        }
    }

    private void replaceValues(ProductVariantOption option, List<ProductVariantOptionValueRequest> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        int fallbackSortOrder = 0;
        for (ProductVariantOptionValueRequest request : values) {
            ProductVariantOptionValue value = new ProductVariantOptionValue();
            value.setCode(generateValueCode(option, request.label()));
            value.setLabel(request.label());
            value.setSwatchValue(request.swatchValue());
            value.setSortOrder(request.sortOrder() != null ? request.sortOrder() : fallbackSortOrder);
            value.setActive(request.active() != null ? request.active() : true);
            option.addValue(value);
            fallbackSortOrder++;
        }
    }

    private String generateOptionCode(String name) {
        return SlugGenerator.generateUniqueCode(
                SlugGenerator.generateCodeFromName(name),
                optionRepository::existsByCode);
    }

    private String generateValueCode(ProductVariantOption option, String label) {
        String baseCode = SlugGenerator.generateCodeFromName(label);
        boolean exists = option.getValues().stream()
                .anyMatch(value -> value.getCode() != null && value.getCode().equals(baseCode));
        if (!exists) {
            return baseCode;
        }
        int suffix = 2;
        String candidate = baseCode + "_" + suffix;
        while (hasValueCode(option, candidate)) {
            suffix++;
            candidate = baseCode + "_" + suffix;
        }
        return candidate;
    }

    private boolean hasValueCode(ProductVariantOption option, String code) {
        return option.getValues().stream().anyMatch(value -> code.equals(value.getCode()));
    }
}
