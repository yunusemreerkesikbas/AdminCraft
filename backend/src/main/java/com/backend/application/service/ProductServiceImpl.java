package com.backend.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.request.ProductI18nDto;
import com.backend.domain.entity.Category;
import com.backend.domain.entity.Media;
import com.backend.domain.entity.Product;
import com.backend.domain.entity.ProductAttribute;
import com.backend.domain.entity.ProductAttributeDefinition;
import com.backend.domain.entity.ProductCategoryLink;
import com.backend.domain.entity.ProductFieldValue;
import com.backend.domain.entity.ProductI18n;
import com.backend.domain.entity.ProductMedia;
import com.backend.domain.entity.ProductType;
import com.backend.domain.entity.ResponsiveMediaSet;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.ProductStatus;
import com.backend.domain.repository.CategoryRepository;
import com.backend.domain.repository.MediaRepository;
import com.backend.domain.repository.ProductAttributeDefinitionRepository;
import com.backend.domain.repository.ProductAttributeRepository;
import com.backend.domain.repository.ProductCategoryLinkRepository;
import com.backend.domain.repository.ProductFieldValueRepository;
import com.backend.domain.repository.ProductI18nRepository;
import com.backend.domain.repository.ProductMediaRepository;
import com.backend.domain.repository.ProductRepository;
import com.backend.domain.repository.ProductTypeRepository;
import com.backend.domain.repository.ResponsiveMediaSetRepository;
import com.backend.domain.enums.ActivityAction;
import com.backend.infrastructure.tenant.TenantContext;
import com.backend.presentation.dto.request.ResponsiveMediaRequest;
import com.backend.shared.common.HtmlSanitizer;
import com.backend.shared.common.SecurityHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductI18nRepository productI18nRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final ProductAttributeDefinitionRepository attributeDefinitionRepository;
    private final ProductCategoryLinkRepository productCategoryLinkRepository;
    private final ProductMediaRepository productMediaRepository;
    private final ProductTypeRepository productTypeRepository;
    private final CategoryRepository categoryRepository;
    private final ResponsiveMediaSetRepository responsiveMediaSetRepository;
    private final MediaRepository mediaRepository;

    private final ProductFieldService productFieldService;
    private final ProductFieldValueRepository productFieldValueRepository;
    private final SiteActivityPublisher activityPublisher;
    private final SecurityHelper securityHelper;

    @Override
    @Transactional
    public Product createComposite(Long productTypeId, String sku, BigDecimal basePrice,
            ProductStatus status, Boolean isVisible, Long responsiveMediaId,
            Map<Language, ProductI18nDto> translations,
            Map<String, Object> attributes,
            List<Long> categoryIds, Long primaryCategoryId,
            List<ResponsiveMediaRequest> gallery,
            Map<String, Object> customFields,
            Long createdBy) {
        TenantContext.validateActive();
        log.debug("Creating product with SKU: {}", sku);

        ProductType productType = productTypeRepository.findById(productTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Product type not found: " + productTypeId));

        if (productRepository.existsBySku(sku)) {
            throw new IllegalArgumentException("Product SKU already exists: " + sku);
        }

        ResponsiveMediaSet responsiveMediaSet = null;
        if (responsiveMediaId != null) {
            responsiveMediaSet = responsiveMediaSetRepository.findById(responsiveMediaId)
                    .orElseThrow(
                            () -> new IllegalArgumentException("Responsive media set not found: " + responsiveMediaId));
        }

        Product product = new Product();
        product.setProductType(productType);
        product.setSku(sku);
        product.setBasePrice(basePrice);
        product.setStatus(status != null ? status : ProductStatus.DRAFT);
        product.setIsVisible(isVisible != null ? isVisible : true);
        product.setResponsiveMediaSet(responsiveMediaSet);
        product.setCreatedBy(createdBy);
        product.setUpdatedBy(createdBy);

        Product saved = productRepository.save(product);

        if (translations != null && !translations.isEmpty()) {
            validateAtLeastOneTranslationName(translations);
            saveTranslations(saved, translations, createdBy);
        } else {
            throw new IllegalArgumentException("At least one translation with name is required");
        }
        saveAttributes(saved, productType, attributes);
        saveCategoryLinks(saved, categoryIds, primaryCategoryId);
        saveGalleryMedia(saved, gallery);

        // Save custom field values (global product fields)
        if (customFields != null && !customFields.isEmpty()) {
            productFieldService.saveFieldValues(saved, customFields);
        }

        log.info("Created product id: {}, sku: {}", saved.getId(), saved.getSku());
        String productName = translations.values().stream()
                .map(dto -> dto.name()).filter(n -> n != null && !n.isBlank())
                .findFirst().orElse(saved.getSku());
        activityPublisher.publishProductEvent(saved.getId(), productName, ActivityAction.CREATED,
                createdBy, null, null);
        return findByIdComposite(saved.getId()).orElse(saved);
    }

    @Override
    @Transactional
    public Product updateComposite(Long id, BigDecimal basePrice,
            ProductStatus status, Boolean isVisible, Long responsiveMediaId,
            Map<Language, ProductI18nDto> translations,
            Map<String, Object> attributes,
            List<Long> categoryIds, Long primaryCategoryId,
            List<ResponsiveMediaRequest> gallery,
            Map<String, Object> customFields,
            Long updatedBy) {
        TenantContext.validateActive();
        log.debug("Updating product id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        if (basePrice != null)
            product.setBasePrice(basePrice);
        if (status != null)
            product.setStatus(status);
        if (isVisible != null)
            product.setIsVisible(isVisible);
        product.setUpdatedBy(updatedBy);

        if (responsiveMediaId != null) {
            ResponsiveMediaSet responsiveMediaSet = responsiveMediaSetRepository.findById(responsiveMediaId)
                    .orElseThrow(
                            () -> new IllegalArgumentException("Responsive media set not found: " + responsiveMediaId));
            product.setResponsiveMediaSet(responsiveMediaSet);
        }

        Product saved = productRepository.save(product);

        if (translations != null && !translations.isEmpty()) {
            validateAtLeastOneTranslationName(translations);
            updateTranslations(saved, translations, updatedBy);
        }

        if (attributes != null) {
            updateAttributes(saved, product.getProductType(), attributes);
        }

        if (categoryIds != null) {
            updateCategoryLinks(saved, categoryIds, primaryCategoryId);
        }

        if (gallery != null) {
            updateGalleryMedia(saved, gallery);
        }

        // Save custom field values (global product fields)
        if (customFields != null && !customFields.isEmpty()) {
            productFieldService.saveFieldValues(saved, customFields);
        }

        log.info("Updated product id: {}, sku: {}", saved.getId(), saved.getSku());
        String productName = translations != null ? translations.values().stream()
                .map(dto -> dto.name()).filter(n -> n != null && !n.isBlank())
                .findFirst().orElse(saved.getSku()) : saved.getSku();
        activityPublisher.publishProductEvent(saved.getId(), productName, ActivityAction.UPDATED,
                updatedBy, null, null);

        // Reload product with productType eagerly fetched
        Product result = productRepository.findByIdComposite(saved.getId())
                .orElseThrow(() -> new IllegalStateException("Product not found after save: " + saved.getId()));

        // Refresh collections with eager fetched data
        List<ProductI18n> i18nContent = productI18nRepository.findByProductId(result.getId());
        result.getI18nContent().clear();
        result.getI18nContent().addAll(i18nContent);

        List<ProductCategoryLink> categoryLinks = productCategoryLinkRepository
                .findByProductIdWithCategories(result.getId());
        result.getCategoryLinks().clear();
        result.getCategoryLinks().addAll(categoryLinks);

        List<ProductMedia> galleryMedia = productMediaRepository.findByProductIdWithMedia(result.getId());
        result.getGallery().clear();
        result.getGallery().addAll(galleryMedia);

        List<ProductAttribute> refreshedAttributes = productAttributeRepository
                .findByProductIdWithDefinitions(result.getId());
        result.getAttributes().clear();
        result.getAttributes().addAll(refreshedAttributes);

        List<ProductFieldValue> refreshedFieldValues = productFieldValueRepository
                .findByProductIdWithDefinition(result.getId());
        result.getFieldValues().clear();
        result.getFieldValues().addAll(refreshedFieldValues);

        return result;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        TenantContext.validateActive();
        log.debug("Deleting product id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        productRepository.delete(product);
        log.info("Deleted product id: {}, sku: {}", id, product.getSku());
        activityPublisher.publishProductEvent(id, product.getSku(), ActivityAction.DELETED,
                safeGetCurrentUserId(), null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        TenantContext.validateActive();
        return productRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findByIdComposite(Long id) {
        TenantContext.validateActive();
        Optional<Product> productOpt = productRepository.findByIdComposite(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            List<ProductI18n> i18nContent = productI18nRepository.findByProductId(product.getId());
            product.getI18nContent().clear();
            if (!i18nContent.isEmpty()) {
                product.getI18nContent().addAll(i18nContent);
            }
            List<ProductCategoryLink> categoryLinks = productCategoryLinkRepository
                    .findByProductIdWithCategories(product.getId());
            product.getCategoryLinks().clear();
            if (!categoryLinks.isEmpty()) {
                product.getCategoryLinks().addAll(categoryLinks);
            }
            List<ProductMedia> gallery = productMediaRepository.findByProductIdWithMedia(product.getId());
            product.getGallery().clear();
            if (!gallery.isEmpty()) {
                product.getGallery().addAll(gallery);
            }
            // Refresh attributes with their definitions
            List<ProductAttribute> attributes = productAttributeRepository
                    .findByProductIdWithDefinitions(product.getId());
            product.getAttributes().clear();
            if (!attributes.isEmpty()) {
                product.getAttributes().addAll(attributes);
            }
            // Refresh custom field values
            List<ProductFieldValue> fieldValues = productFieldValueRepository
                    .findByProductIdWithDefinition(product.getId());
            product.getFieldValues().clear();
            if (!fieldValues.isEmpty()) {
                product.getFieldValues().addAll(fieldValues);
            }
        }
        return productOpt;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findByUid(String uid) {
        TenantContext.validateActive();
        return productRepository.findByUid(uid);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findBySku(String sku) {
        TenantContext.validateActive();
        return productRepository.findBySku(sku);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        TenantContext.validateActive();
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findAllPaged(Pageable pageable) {
        TenantContext.validateActive();
        return productRepository.findAllPaged(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> search(String query, ProductStatus status, Long categoryId, Pageable pageable) {
        TenantContext.validateActive();
        return productRepository.searchWithFilters(query, status, categoryId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findByCategoryId(Long categoryId) {
        TenantContext.validateActive();
        return productRepository.findByCategoryId(categoryId);
    }

    @Override
    @Transactional
    public Product updateStatus(Long id, ProductStatus status, Long updatedBy) {
        TenantContext.validateActive();
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        product.setStatus(status);
        product.setUpdatedBy(updatedBy);
        Product saved = productRepository.save(product);
        ActivityAction action = (status == ProductStatus.PUBLISHED) ? ActivityAction.PUBLISHED
                : ActivityAction.UPDATED;
        activityPublisher.publishProductEvent(id, saved.getSku(), action, updatedBy, null, null);
        return saved;
    }

    @Override
    @Transactional
    public Product updateVisibility(Long id, Boolean isVisible, Long updatedBy) {
        TenantContext.validateActive();
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        product.setIsVisible(isVisible);
        product.setUpdatedBy(updatedBy);
        return productRepository.save(product);
    }

    private void validateAtLeastOneTranslationName(Map<Language, ProductI18nDto> translations) {
        boolean hasAtLeastOneName = translations.values().stream()
                .anyMatch(dto -> dto.name() != null && !dto.name().trim().isEmpty());
        if (!hasAtLeastOneName) {
            throw new IllegalArgumentException("At least one translation must have a name");
        }
    }

    private void saveTranslations(Product product, Map<Language, ProductI18nDto> translations, Long createdBy) {
        List<ProductI18n> i18nList = translations.entrySet().stream()
                .filter(entry -> {
                    String name = entry.getValue().name();
                    return name != null && !name.trim().isEmpty();
                })
                .map(entry -> {
                    ProductI18n i18n = new ProductI18n();
                    i18n.setProduct(product);
                    i18n.setLanguage(entry.getKey());
                    i18n.setName(entry.getValue().name());
                    i18n.setShortDescription(HtmlSanitizer.sanitizeRichText(entry.getValue().shortDescription()));
                    i18n.setDescription(HtmlSanitizer.sanitizeRichText(entry.getValue().description()));
                    i18n.setSeoTitle(entry.getValue().seoTitle());
                    i18n.setSeoDescription(entry.getValue().seoDescription());
                    i18n.setCreatedBy(createdBy);
                    i18n.setUpdatedBy(createdBy);
                    return i18n;
                })
                .toList();
        if (!i18nList.isEmpty()) {
            productI18nRepository.saveAll(i18nList);
        }
    }

    private void updateTranslations(Product product, Map<Language, ProductI18nDto> translations, Long updatedBy) {
        translations.entrySet().forEach(entry -> {
            String name = entry.getValue().name();
            boolean hasName = name != null && !name.trim().isEmpty();

            productI18nRepository.findByProductIdAndLanguage(product.getId(), entry.getKey())
                    .ifPresentOrElse(
                            existingI18n -> {
                                if (hasName) {
                                    existingI18n.setName(name);
                                    existingI18n.setShortDescription(
                                            HtmlSanitizer.sanitizeRichText(entry.getValue().shortDescription()));
                                    existingI18n.setDescription(
                                            HtmlSanitizer.sanitizeRichText(entry.getValue().description()));
                                    existingI18n.setSeoTitle(entry.getValue().seoTitle());
                                    existingI18n.setSeoDescription(entry.getValue().seoDescription());
                                    existingI18n.setUpdatedBy(updatedBy);
                                    productI18nRepository.save(existingI18n);
                                } else {
                                    productI18nRepository.delete(existingI18n);
                                }
                            },
                            () -> {
                                if (hasName) {
                                    ProductI18n newI18n = new ProductI18n();
                                    newI18n.setProduct(product);
                                    newI18n.setLanguage(entry.getKey());
                                    newI18n.setName(name);
                                    newI18n.setShortDescription(
                                            HtmlSanitizer.sanitizeRichText(entry.getValue().shortDescription()));
                                    newI18n.setDescription(
                                            HtmlSanitizer.sanitizeRichText(entry.getValue().description()));
                                    newI18n.setSeoTitle(entry.getValue().seoTitle());
                                    newI18n.setSeoDescription(entry.getValue().seoDescription());
                                    newI18n.setCreatedBy(updatedBy);
                                    newI18n.setUpdatedBy(updatedBy);
                                    productI18nRepository.save(newI18n);
                                }
                            });
        });
    }

    private void saveAttributes(Product product, ProductType productType, Map<String, Object> attributes) {
        List<ProductAttributeDefinition> definitions = attributeDefinitionRepository
                .findByProductTypeId(productType.getId());

        if (attributes == null || attributes.isEmpty())
            return;

        for (ProductAttributeDefinition def : definitions) {
            if (attributes.containsKey(def.getCode())) {
                Object value = attributes.get(def.getCode());
                saveAttribute(product, def, value);
            }
        }
    }

    private void updateAttributes(Product product, ProductType productType, Map<String, Object> attributes) {
        productAttributeRepository.deleteByProductId(product.getId());
        saveAttributes(product, productType, attributes);
    }

    private boolean isValueEmpty(Object value) {
        if (value == null)
            return true;
        if (value instanceof String str)
            return str.trim().isEmpty();
        return false;
    }

    private void saveAttribute(Product product, ProductAttributeDefinition def, Object value) {
        if (isValueEmpty(value)) {
            return;
        }

        ProductAttribute attr = new ProductAttribute();
        attr.setProduct(product);
        attr.setAttributeDefinition(def);

        switch (def.getFieldType()) {
            case TEXT, RICHTEXT -> attr.setValueText(String.valueOf(value));
            case NUMBER -> {
                if (value instanceof Number num) {
                    attr.setValueNumber(BigDecimal.valueOf(num.doubleValue()));
                } else {
                    attr.setValueNumber(new BigDecimal(String.valueOf(value)));
                }
            }
            case BOOLEAN -> {
                if (value instanceof Boolean bool) {
                    attr.setValueBoolean(bool);
                } else {
                    attr.setValueBoolean(Boolean.parseBoolean(String.valueOf(value)));
                }
            }
            case DATE -> {
                if (value instanceof LocalDate date) {
                    attr.setValueDate(date);
                } else {
                    attr.setValueDate(LocalDate.parse(String.valueOf(value), DateTimeFormatter.ISO_LOCAL_DATE));
                }
            }
            case MEDIA -> {
                if (value instanceof Number num) {
                    Media media = mediaRepository.findById(num.longValue()).orElse(null);
                    attr.setValueMedia(media);
                }
            }
        }

        productAttributeRepository.save(attr);
    }

    private void saveCategoryLinks(Product product, List<Long> categoryIds, Long primaryCategoryId) {
        if (categoryIds == null || categoryIds.isEmpty())
            return;

        for (Long categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

            ProductCategoryLink link = new ProductCategoryLink(product, category, categoryId.equals(primaryCategoryId));
            productCategoryLinkRepository.save(link);
        }
    }

    private void updateCategoryLinks(Product product, List<Long> categoryIds, Long primaryCategoryId) {
        productCategoryLinkRepository.deleteByProductId(product.getId());
        saveCategoryLinks(product, categoryIds, primaryCategoryId);
    }

    private void saveGalleryMedia(Product product, List<ResponsiveMediaRequest> gallery) {
        if (gallery == null || gallery.isEmpty())
            return;

        int sortOrder = 0;
        for (ResponsiveMediaRequest req : gallery) {
            Media desktop = mediaRepository.findById(req.desktopMediaId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Desktop media not found: " + req.desktopMediaId()));

            Media mobile = null;
            if (req.mobileMediaId() != null) {
                mobile = mediaRepository.findById(req.mobileMediaId())
                        .orElseThrow(
                                () -> new IllegalArgumentException("Mobile media not found: " + req.mobileMediaId()));
            } else {
                mobile = desktop;
            }
            ResponsiveMediaSet responsiveSet = new ResponsiveMediaSet();
            responsiveSet.setUid("rm_" + System.nanoTime());
            responsiveSet.setCode("gallery_" + product.getSku() + "_" + sortOrder + "_" + System.currentTimeMillis());
            responsiveSet.setDesktopMedia(desktop);
            responsiveSet.setMobileMedia(mobile);

            responsiveMediaSetRepository.save(responsiveSet);

            ProductMedia pm = new ProductMedia();
            pm.setProduct(product);
            pm.setResponsiveMediaSet(responsiveSet);
            pm.setMediaType(com.backend.domain.enums.ProductMediaType.GALLERY);
            pm.setSortOrder(sortOrder++);
            productMediaRepository.save(pm);
        }
    }

    private void updateGalleryMedia(Product product, List<ResponsiveMediaRequest> gallery) {
        productMediaRepository.deleteByProductId(product.getId());
        saveGalleryMedia(product, gallery);
    }

    private Long safeGetCurrentUserId() {
        try {
            return securityHelper.getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }
}
