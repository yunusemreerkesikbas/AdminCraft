package com.backend.presentation.controller;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.dto.request.ProductI18nDto;
import com.backend.application.service.ProductService;
import com.backend.domain.entity.Category;
import com.backend.domain.entity.CategoryI18n;
import com.backend.domain.entity.Product;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.ProductStatus;
import com.backend.domain.repository.ProductCategoryLinkRepository;
import com.backend.infrastructure.tenant.TenantContext;
import com.backend.presentation.dto.request.ProductCompositeRequest;
import com.backend.presentation.dto.request.ProductI18nRequest;
import com.backend.presentation.dto.request.ProductUpdateRequest;
import com.backend.presentation.dto.response.PageableResponse;
import com.backend.presentation.dto.response.ProductCompositeResponse;
import com.backend.presentation.dto.response.ProductListItemResponse;
import com.backend.presentation.dto.response.SortConfig;
import com.backend.presentation.dto.response.SortOptionDto;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityUtil;
import com.backend.shared.common.SortParseUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasRole('TENANT_ADMIN')")
@Tag(name = "Product", description = "Endpoints for managing products")
public class ProductController {

        private final ProductService productService;
        private final MessageSource messageSource;
        private final ProductCategoryLinkRepository productCategoryLinkRepository;
        private final TenantContext tenantContext;

        @GetMapping
        @Operation(summary = "List products", description = "Retrieves products with pagination, search and filters")
        public ResponseEntity<ApiResponse<PageableResponse<ProductListItemResponse>>> list(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
                        @RequestParam(required = false) String sort,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) ProductStatus status,
                        @RequestParam(required = false) Long categoryId,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        Language language = Language.fromCodeOrDefault(lang);
                        String effectiveSort = sort != null ? sort : "createdAt,desc";
                        Sort sortObj = SortParseUtil.parse(effectiveSort,
                                        Set.of("sku", "basePrice", "status", "createdAt"),
                                        "createdAt,desc");
                        PageRequest pageRequest = PageRequest.of(page, size, sortObj);

                        Page<Product> products = productService.search(search, status, categoryId, pageRequest);

                        List<Long> productIds = products.getContent().stream()
                                        .map(Product::getId)
                                        .toList();

                        Map<Long, String> primaryCategoryNames = productCategoryLinkRepository
                                        .findPrimaryByProductIdIn(productIds)
                                        .stream()
                                        .collect(Collectors.toMap(
                                                        link -> link.getId().getProductId(),
                                                        link -> {
                                                                Category category = link.getCategory();
                                                                if (category.getI18nContent() != null && !category
                                                                                .getI18nContent().isEmpty()) {
                                                                        return category.getI18nContent().stream()
                                                                                        .filter(i -> i.getLanguage() == language)
                                                                                        .findFirst()
                                                                                        .map(CategoryI18n::getName)
                                                                                        .orElse(category.getCode());
                                                                }
                                                                return category.getCode();
                                                        },
                                                        (existing, replacement) -> existing));

                        Page<ProductListItemResponse> responsePage = products.map(p -> ProductListItemResponse.from(p,
                                        language, tenantContext.getCurrency(), primaryCategoryNames.get(p.getId())));

                        SortConfig sortConfig = SortConfig.of(effectiveSort, List.of(
                                        SortOptionDto.of("sku,asc", "admin.sort.sku"),
                                        SortOptionDto.of("basePrice,asc", "admin.sort.price.asc"),
                                        SortOptionDto.of("basePrice,desc", "admin.sort.price.desc"),
                                        SortOptionDto.defaultOption("createdAt,desc", "admin.sort.newest")));
                        PageableResponse<ProductListItemResponse> response = PageableResponse.from(responsePage,
                                        sortConfig);

                        return ResponseEntity.ok(ApiResponse.success(response));
                } catch (Exception ex) {
                        log.error("Error listing products: {}", ex.getMessage());
                        String msg = messageSource.getMessage("product.list.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(msg));
                }
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get product by ID", description = "Retrieves a product. Use 'include=translations' to get all translations")
        public ResponseEntity<ApiResponse<ProductCompositeResponse>> getById(
                        @Parameter(description = "Product ID") @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestParam(value = "include", required = false) String include,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        boolean includeTranslations = include != null && include.contains("translations");

                        return productService.findByIdComposite(id)
                                        .map(p -> ResponseEntity
                                                        .ok(ApiResponse.success(ProductCompositeResponse.from(p, tenantContext.getCurrency()))))
                                        .orElseGet(() -> {
                                                String msg = messageSource.getMessage("product.not.found",
                                                                new Object[] { id }, Locale.forLanguageTag(lang));
                                                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                                .body(ApiResponse.error(msg));
                                        });
                } catch (Exception ex) {
                        log.error("Error getting product {}: {}", id, ex.getMessage());
                        String msg = messageSource.getMessage("product.get.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(msg));
                }
        }

        @PostMapping("/composite")
        @Operation(summary = "Create product", description = "Creates a new product with translations, attributes, categories and gallery")
        public ResponseEntity<ApiResponse<ProductCompositeResponse>> createComposite(
                        @RequestBody @Valid ProductCompositeRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        Long userId = SecurityUtil.getCurrentUserIdOrThrow();
                        Product created = productService.createComposite(
                                        request.productTypeId(),
                                        request.sku(),
                                        request.basePrice(),
                                        request.status(),
                                        request.isVisible(),
                                        request.responsiveMediaId(),
                                        mapProductTranslations(request.translations()),
                                        request.attributes(),
                                        request.categoryIds(),
                                        request.primaryCategoryId(),
                                        request.gallery(),
                                        userId);

                        String msg = messageSource.getMessage("product.create.success", null,
                                        Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.success(msg, ProductCompositeResponse.from(created, tenantContext.getCurrency())));
                } catch (IllegalArgumentException ex) {
                        log.warn("Product creation validation error: {}", ex.getMessage());
                        String msg = messageSource.getMessage("product.create.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
                } catch (Exception ex) {
                        log.error("Error creating product: {}", ex.getMessage());
                        String msg = messageSource.getMessage("product.create.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
                }
        }

        @PutMapping("/{id}/composite")
        @Operation(summary = "Update product", description = "Updates an existing product with translations, attributes, categories and gallery")
        public ResponseEntity<ApiResponse<ProductCompositeResponse>> updateComposite(
                        @Parameter(description = "Product ID") @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestBody @Valid ProductUpdateRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        Long userId = SecurityUtil.getCurrentUserIdOrThrow();
                        Product updated = productService.updateComposite(
                                        id,
                                        request.basePrice(),
                                        request.status(),
                                        request.isVisible(),
                                        request.responsiveMediaId(),
                                        mapProductTranslations(request.translations()),
                                        request.attributes(),
                                        request.categoryIds(),
                                        request.primaryCategoryId(),
                                        request.gallery(),
                                        userId);

                        String msg = messageSource.getMessage("product.update.success", null,
                                        Locale.forLanguageTag(lang));
                        return ResponseEntity.ok(ApiResponse.success(msg, ProductCompositeResponse.from(updated, tenantContext.getCurrency())));
                } catch (IllegalArgumentException ex) {
                        log.warn("Product update validation error: {}", ex.getMessage());
                        String msg = messageSource.getMessage("product.update.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
                } catch (Exception ex) {
                        log.error("Error updating product {}: {}", id, ex.getMessage());
                        String msg = messageSource.getMessage("product.update.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
                }
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete product", description = "Deletes a product and all its related data")
        public ResponseEntity<ApiResponse<Void>> delete(
                        @Parameter(description = "Product ID") @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        productService.delete(id);
                        String msg = messageSource.getMessage("product.delete.success", null,
                                        Locale.forLanguageTag(lang));
                        return ResponseEntity.ok(ApiResponse.success(msg, null));
                } catch (IllegalArgumentException ex) {
                        log.warn("Product delete validation error: {}", ex.getMessage());
                        String msg = messageSource.getMessage("product.delete.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
                } catch (Exception ex) {
                        log.error("Error deleting product {}: {}", id, ex.getMessage());
                        String msg = messageSource.getMessage("product.delete.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
                }
        }

        @PatchMapping("/{id}/status")
        @Operation(summary = "Update product status", description = "Updates the status of a product (DRAFT/PUBLISHED)")
        public ResponseEntity<ApiResponse<ProductListItemResponse>> updateStatus(
                        @Parameter(description = "Product ID") @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestParam @NotNull ProductStatus status,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        Long userId = SecurityUtil.getCurrentUserIdOrThrow();
                        Language language = Language.fromCodeOrDefault(lang);
                        Product updated = productService.updateStatus(id, status, userId);

                        String msg = messageSource.getMessage("product.status.update.success", null,
                                        Locale.forLanguageTag(lang));
                        return ResponseEntity
                                        .ok(ApiResponse.success(msg, ProductListItemResponse.from(updated, language, tenantContext.getCurrency())));
                } catch (IllegalArgumentException ex) {
                        log.warn("Product status update validation error: {}", ex.getMessage());
                        String msg = messageSource.getMessage("product.status.update.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
                } catch (Exception ex) {
                        log.error("Error updating product status {}: {}", id, ex.getMessage());
                        String msg = messageSource.getMessage("product.status.update.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
                }
        }

        @PatchMapping("/{id}/visibility")
        @Operation(summary = "Update product visibility", description = "Updates the visibility of a product")
        public ResponseEntity<ApiResponse<ProductListItemResponse>> updateVisibility(
                        @Parameter(description = "Product ID") @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestParam @NotNull Boolean isVisible,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        Long userId = SecurityUtil.getCurrentUserIdOrThrow();
                        Language language = Language.fromCodeOrDefault(lang);
                        Product updated = productService.updateVisibility(id, isVisible, userId);

                        String msg = messageSource.getMessage("product.visibility.update.success", null,
                                        Locale.forLanguageTag(lang));
                        return ResponseEntity
                                        .ok(ApiResponse.success(msg, ProductListItemResponse.from(updated, language, tenantContext.getCurrency())));
                } catch (IllegalArgumentException ex) {
                        log.warn("Product visibility update validation error: {}", ex.getMessage());
                        String msg = messageSource.getMessage("product.visibility.update.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
                } catch (Exception ex) {
                        log.error("Error updating product visibility {}: {}", id, ex.getMessage());
                        String msg = messageSource.getMessage("product.visibility.update.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
                }
        }

        private Map<Language, ProductI18nDto> mapProductTranslations(
                        Map<Language, ProductI18nRequest> translations) {
                if (translations == null)
                        return null;
                return translations.entrySet().stream()
                                .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                e -> new ProductI18nDto(
                                                                e.getValue().name(),
                                                                e.getValue().shortDescription(),
                                                                e.getValue().description(),
                                                                e.getValue().seoTitle(),
                                                                e.getValue().seoDescription())));
        }
}
