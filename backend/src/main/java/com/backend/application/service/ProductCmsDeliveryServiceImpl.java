package com.backend.application.service;

import com.backend.application.dto.delivery.CategoryDeliveryResponse;
import com.backend.application.dto.delivery.ProductDeliveryResponse;
import com.backend.application.dto.delivery.ProductListDeliveryResponse;
import com.backend.domain.entity.Category;
import com.backend.domain.entity.Product;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.ProductStatus;
import com.backend.domain.repository.CategoryRepository;
import com.backend.domain.repository.ProductRepository;
import com.backend.infrastructure.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCmsDeliveryServiceImpl implements ProductCmsDeliveryService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductDeliveryResponse> getProductByUid(String uid, Language language) {
        log.debug("CMS Delivery: Fetching product uid={}, lang={}", uid, language);

        return productRepository.findByUid(uid)
                .filter(p -> p.getStatus() == ProductStatus.PUBLISHED)
                .filter(p -> p.getIsVisible() != null && p.getIsVisible())
                .map(p -> {
                    Product full = productRepository.findByIdComposite(p.getId()).orElse(p);
                    return ProductDeliveryResponse.from(full, language, tenantContext.getCurrency());
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDeliveryResponse> getProductsByUids(List<String> uids, Language language) {
        log.debug("CMS Delivery: Fetching {} products, lang={}", uids.size(), language);

        return uids.stream()
                .map(uid -> getProductByUid(uid, language))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListDeliveryResponse> getProductsByCategory(String categoryUid, Language language, Pageable pageable) {
        log.debug("CMS Delivery: Fetching products by category uid={}, lang={}", categoryUid, language);

        return categoryRepository.findByUid(categoryUid)
                .map(category -> {
                    Page<Product> products = productRepository.findPublishedByCategoryId(category.getId(), pageable);
                    return products.map(p -> ProductListDeliveryResponse.from(p, language, tenantContext.getCurrency()));
                })
                .orElse(Page.empty());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListDeliveryResponse> searchProducts(String query, Language language, Pageable pageable) {
        log.debug("CMS Delivery: Searching products query={}, lang={}", query, language);

        Page<Product> products = productRepository.searchPublished(query, pageable);
        return products.map(p -> ProductListDeliveryResponse.from(p, language, tenantContext.getCurrency()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDeliveryResponse> getCategoryByUid(String uid, Language language) {
        log.debug("CMS Delivery: Fetching category uid={}, lang={}", uid, language);

        return categoryRepository.findByUid(uid)
                .filter(c -> c.getIsVisible() != null && c.getIsVisible())
                .map(c -> {
                    Category full = categoryRepository.findByIdWithI18n(c.getId()).orElse(c);
                    return CategoryDeliveryResponse.from(full, language);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDeliveryResponse> getCategoryTree(Language language) {
        log.debug("CMS Delivery: Fetching category tree, lang={}", language);

        List<Category> roots = categoryRepository.findRootCategories();
        return roots.stream()
                .filter(c -> c.getIsVisible() != null && c.getIsVisible())
                .map(c -> CategoryDeliveryResponse.from(c, language))
                .toList();
    }
}
