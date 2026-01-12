package com.backend.presentation.controller;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.dto.request.CategoryI18nDto;
import com.backend.application.service.CategoryService;
import com.backend.domain.entity.Category;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.CategoryCompositeRequest;
import com.backend.presentation.dto.request.CategoryI18nRequest;
import com.backend.presentation.dto.request.CategoryUpdateRequest;
import com.backend.presentation.dto.response.CategoryCompositeResponse;
import com.backend.presentation.dto.response.CategoryResponse;
import com.backend.presentation.dto.response.CategoryTreeResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/products/categories")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasRole('TENANT_ADMIN')")
@Tag(name = "Category", description = "Endpoints for managing product categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final MessageSource messageSource;

    @GetMapping
    @Operation(summary = "Get category tree", description = "Retrieves all categories as a tree structure")
    public ResponseEntity<ApiResponse<List<CategoryTreeResponse>>> getTree(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            Language language = Language.fromCodeOrDefault(lang);
            List<Category> roots = categoryService.getTree();
            List<CategoryTreeResponse> tree = roots.stream()
                    .map(c -> CategoryTreeResponse.fromAll(c, language))
                    .toList();
            return ResponseEntity.ok(ApiResponse.success(tree));
        } catch (Exception ex) {
            log.error("Error getting category tree: {}", ex.getMessage());
            String msg = messageSource.getMessage("category.list.error",
                    new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(msg));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieves a category by its ID")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(
            @Parameter(description = "Category ID") @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            Language language = Language.fromCodeOrDefault(lang);
            return categoryService.findByIdWithI18n(id)
                    .map(c -> ResponseEntity.ok(ApiResponse.success(CategoryResponse.from(c, language))))
                    .orElseGet(() -> {
                        String msg = messageSource.getMessage("category.not.found",
                                new Object[] { id }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(msg));
                    });
        } catch (Exception ex) {
            log.error("Error getting category {}: {}", id, ex.getMessage());
            String msg = messageSource.getMessage("category.get.error",
                    new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(msg));
        }
    }

    @GetMapping("/{id}/composite")
    @Operation(summary = "Get category composite", description = "Retrieves a category with all translations")
    public ResponseEntity<ApiResponse<CategoryCompositeResponse>> getComposite(
            @Parameter(description = "Category ID") @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            return categoryService.findByIdWithI18n(id)
                    .map(c -> ResponseEntity.ok(ApiResponse.success(CategoryCompositeResponse.from(c))))
                    .orElseGet(() -> {
                        String msg = messageSource.getMessage("category.not.found",
                                new Object[] { id }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(msg));
                    });
        } catch (Exception ex) {
            log.error("Error getting category composite {}: {}", id, ex.getMessage());
            String msg = messageSource.getMessage("category.get.error",
                    new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(msg));
        }
    }

    @PostMapping("/composite")
    @Operation(summary = "Create category", description = "Creates a new category with translations")
    public ResponseEntity<ApiResponse<CategoryCompositeResponse>> createComposite(
            @RequestBody @Valid CategoryCompositeRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            Long userId = SecurityUtil.getCurrentUserIdOrThrow();
            Category created = categoryService.createComposite(
                    request.code(), request.parentId(), request.sortOrder(),
                    request.isVisible(), mapCategoryTranslations(request.translations()), userId);

            String msg = messageSource.getMessage("category.create.success", null, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(msg, CategoryCompositeResponse.from(created)));
        } catch (IllegalArgumentException ex) {
            log.warn("Category creation validation error: {}", ex.getMessage());
            String msg = messageSource.getMessage("category.create.error",
                    new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
        } catch (Exception ex) {
            log.error("Error creating category: {}", ex.getMessage());
            String msg = messageSource.getMessage("category.create.error",
                    new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
        }
    }

    @PutMapping("/{id}/composite")
    @Operation(summary = "Update category", description = "Updates an existing category with translations")
    public ResponseEntity<ApiResponse<CategoryCompositeResponse>> updateComposite(
            @Parameter(description = "Category ID") @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestBody @Valid CategoryUpdateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            Long userId = SecurityUtil.getCurrentUserIdOrThrow();
            Category updated = categoryService.updateComposite(
                    id, request.parentId(), request.sortOrder(),
                    request.isVisible(), mapCategoryTranslations(request.translations()), userId);

            String msg = messageSource.getMessage("category.update.success", null, Locale.forLanguageTag(lang));
            return ResponseEntity.ok(ApiResponse.success(msg, CategoryCompositeResponse.from(updated)));
        } catch (IllegalArgumentException ex) {
            log.warn("Category update validation error: {}", ex.getMessage());
            String msg = messageSource.getMessage("category.update.error",
                    new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
        } catch (Exception ex) {
            log.error("Error updating category {}: {}", id, ex.getMessage());
            String msg = messageSource.getMessage("category.update.error",
                    new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Deletes a category (fails if has children or products)")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Category ID") @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            categoryService.delete(id);
            String msg = messageSource.getMessage("category.delete.success", null, Locale.forLanguageTag(lang));
            return ResponseEntity.ok(ApiResponse.success(msg, null));
        } catch (IllegalArgumentException ex) {
            log.warn("Category delete validation error: {}", ex.getMessage());
            String msg = messageSource.getMessage("category.delete.error",
                    new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
        } catch (IllegalStateException ex) {
            log.warn("Category delete state error: {}", ex.getMessage());
            String msg = messageSource.getMessage("category.delete.restricted",
                    new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(msg));
        } catch (Exception ex) {
            log.error("Error deleting category {}: {}", id, ex.getMessage());
            String msg = messageSource.getMessage("category.delete.error",
                    new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
        }
    }

    private Map<Language, CategoryI18nDto> mapCategoryTranslations(
            Map<Language, CategoryI18nRequest> translations) {
        if (translations == null)
            return null;
        return translations.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new CategoryI18nDto(
                                e.getValue().name(),
                                e.getValue().description())));
    }
}
