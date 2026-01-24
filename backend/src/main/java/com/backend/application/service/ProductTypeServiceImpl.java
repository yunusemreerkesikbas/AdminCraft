package com.backend.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.ProductAttributeDefinition;
import com.backend.domain.entity.ProductType;
import com.backend.domain.enums.ProductFieldType;
import com.backend.domain.exception.BusinessRuleViolationException;
import com.backend.domain.repository.ProductAttributeDefinitionRepository;
import com.backend.domain.repository.ProductRepository;
import com.backend.domain.repository.ProductTypeRepository;
import com.backend.infrastructure.tenant.TenantContext;
import com.backend.shared.util.SlugGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductTypeServiceImpl implements ProductTypeService {

    private final ProductTypeRepository productTypeRepository;
    private final ProductAttributeDefinitionRepository attributeDefinitionRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductType create(String code, String name, String category, Long createdBy) {
        TenantContext.validateActive();
        log.debug("Creating product type with code: {}, name: {}", code, name);

        if (productTypeRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Product type code already exists: " + code);
        }

        ProductType productType = new ProductType();
        productType.setCode(code);
        productType.setName(name);
        productType.setCategory(category);
        productType.setCreatedBy(createdBy);
        productType.setUpdatedBy(createdBy);

        ProductType saved = productTypeRepository.save(productType);
        log.info("Created product type id: {}, code: {}", saved.getId(), saved.getCode());
        return saved;
    }

    @Override
    @Transactional
    public ProductType update(Long id, String name, String category, Long updatedBy) {
        TenantContext.validateActive();
        log.debug("Updating product type id: {} with name: {}", id, name);

        ProductType productType = productTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product type not found with id: " + id));

        productType.setName(name);
        productType.setCategory(category);
        productType.setUpdatedBy(updatedBy);

        ProductType saved = productTypeRepository.save(productType);
        log.info("Updated product type id: {}, code: {}", saved.getId(), saved.getCode());
        return saved;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        TenantContext.validateActive();
        log.debug("Deleting product type id: {}", id);

        ProductType productType = productTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product type not found with id: " + id));

        // Check if any products are using this product type
        long productCount = productRepository.countByProductTypeId(id);
        if (productCount > 0) {
            throw new BusinessRuleViolationException(
                    String.format("Cannot delete product type '%s' because it has %d product(s). " +
                            "Please delete or reassign the products first.",
                            productType.getName(), productCount));
        }

        productTypeRepository.delete(productType);
        log.info("Deleted product type id: {}, code: {}", id, productType.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductType> findById(Long id) {
        TenantContext.validateActive();
        return productTypeRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductType> findByIdWithAttributes(Long id) {
        TenantContext.validateActive();
        return productTypeRepository.findByIdWithAttributes(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductType> findByUid(String uid) {
        TenantContext.validateActive();
        return productTypeRepository.findByUid(uid);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductType> findByCode(String code) {
        TenantContext.validateActive();
        return productTypeRepository.findByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductType> findAll() {
        TenantContext.validateActive();
        return productTypeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductType> findAllPaged(Pageable pageable) {
        TenantContext.validateActive();
        return productTypeRepository.findAllPaged(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductType> search(String query, Pageable pageable) {
        TenantContext.validateActive();
        if (query == null || query.trim().length() < 2) {
            return productTypeRepository.findAllPaged(pageable);
        }
        return productTypeRepository.searchPaged(query.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductType> findByCategory(String category) {
        TenantContext.validateActive();
        return productTypeRepository.findByCategory(category);
    }

    @Override
    @Transactional
    public ProductAttributeDefinition addAttribute(Long productTypeId, String name,
            ProductFieldType fieldType) {
        TenantContext.validateActive();
        log.debug("Adding attribute to product type {}", productTypeId);

        ProductType productType = productTypeRepository.findById(productTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Product type not found with id: " + productTypeId));

        String code = SlugGenerator.generateUniqueCode(
                SlugGenerator.generateCodeFromName(name),
                (c) -> attributeDefinitionRepository.existsByProductTypeIdAndCode(productTypeId, c)
        );

        ProductAttributeDefinition attribute = new ProductAttributeDefinition();
        attribute.setProductType(productType);
        attribute.setCode(code);
        attribute.setName(name);
        attribute.setFieldType(fieldType);

        ProductAttributeDefinition saved = attributeDefinitionRepository.save(attribute);
        log.info("Added attribute id: {}, code: {} to product type {}", saved.getId(), saved.getCode(), productTypeId);
        return saved;
    }

    @Override
    @Transactional
    public ProductAttributeDefinition updateAttribute(Long productTypeId, Long attributeId,
            String name, ProductFieldType fieldType) {
        TenantContext.validateActive();
        log.debug("Updating attribute {} in product type {}", attributeId, productTypeId);

        ProductAttributeDefinition attribute = attributeDefinitionRepository.findById(attributeId)
                .orElseThrow(() -> new IllegalArgumentException("Attribute not found with id: " + attributeId));

        if (!attribute.getProductType().getId().equals(productTypeId)) {
            throw new IllegalArgumentException("Attribute does not belong to this product type");
        }

        attribute.setName(name);
        attribute.setFieldType(fieldType);

        ProductAttributeDefinition saved = attributeDefinitionRepository.save(attribute);
        log.info("Updated attribute id: {}, code: {}", saved.getId(), saved.getCode());
        return saved;
    }

    @Override
    @Transactional
    public void deleteAttribute(Long productTypeId, Long attributeId) {
        TenantContext.validateActive();
        log.debug("Deleting attribute {} from product type {}", attributeId, productTypeId);

        ProductAttributeDefinition attribute = attributeDefinitionRepository.findById(attributeId)
                .orElseThrow(() -> new IllegalArgumentException("Attribute not found with id: " + attributeId));

        if (!attribute.getProductType().getId().equals(productTypeId)) {
            throw new IllegalArgumentException("Attribute does not belong to this product type");
        }

        attributeDefinitionRepository.delete(attribute);
        log.info("Deleted attribute id: {}, code: {}", attributeId, attribute.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductAttributeDefinition> getAttributes(Long productTypeId) {
        TenantContext.validateActive();
        return attributeDefinitionRepository.findByProductTypeId(productTypeId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getProductCount(Long productTypeId) {
        TenantContext.validateActive();
        return productRepository.countByProductTypeId(productTypeId);
    }
}
