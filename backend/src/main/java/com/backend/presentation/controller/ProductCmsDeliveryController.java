package com.backend.presentation.controller;

import com.backend.application.dto.delivery.CategoryDeliveryResponse;
import com.backend.application.dto.delivery.ProductDeliveryResponse;
import com.backend.application.dto.delivery.ProductListDeliveryResponse;
import com.backend.application.service.ProductCmsDeliveryService;
import com.backend.domain.enums.Language;
import com.backend.infrastructure.tenant.TenantContext;
import com.backend.shared.common.ApiResponse;
import com.google.common.util.concurrent.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/cms/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CMS Product Delivery", description = "Public endpoints for product and category delivery")
public class ProductCmsDeliveryController {

    private static final int MAX_BATCH_SIZE = 50;
    private static final double PERMITS_PER_MINUTE = 100.0;
    private static final double PERMITS_PER_SECOND = PERMITS_PER_MINUTE / 60.0;
    private final ConcurrentHashMap<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();

    private final ProductCmsDeliveryService productCmsDeliveryService;
    private final MessageSource messageSource;
    private final TenantContext tenantContext;

    @GetMapping("/{uid}")
    @Operation(summary = "Get product by UID", description = "Retrieves a published product by its UID for public delivery")
    public ResponseEntity<ApiResponse<ProductDeliveryResponse>> getProductByUid(
            @PathVariable String uid,
            @RequestParam(required = false) Language lang,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

        Locale locale = Locale.forLanguageTag(acceptLanguage);
        ResponseEntity<ApiResponse<ProductDeliveryResponse>> rateLimitResponse = checkRateLimit(locale);
        if (rateLimitResponse != null) return rateLimitResponse;

        Language resolvedLang = resolveLanguage(lang, acceptLanguage);
        log.debug("CMS Delivery: Fetching product uid={}, lang={}", uid, resolvedLang);

        return productCmsDeliveryService.getProductByUid(uid, resolvedLang)
                .map(response -> ResponseEntity.ok(
                        ApiResponse.success(messageSource.getMessage("cms.product.found", null, locale), response)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.error(messageSource.getMessage("cms.product.not.found", null, locale))));
    }

    @GetMapping
    @Operation(summary = "Get products by UIDs", description = "Retrieves multiple published products by their UIDs")
    public ResponseEntity<ApiResponse<List<ProductDeliveryResponse>>> getProductsByUids(
            @RequestParam List<String> uids,
            @RequestParam(required = false) Language lang,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

        Locale locale = Locale.forLanguageTag(acceptLanguage);
        ResponseEntity<ApiResponse<List<ProductDeliveryResponse>>> rateLimitResponse = checkRateLimit(locale);
        if (rateLimitResponse != null) return rateLimitResponse;

        if (uids == null || uids.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(messageSource.getMessage("cms.uids.required", null, locale)));
        }

        if (uids.size() > MAX_BATCH_SIZE) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(messageSource.getMessage("cms.uids.limit.exceeded",
                            new Object[]{MAX_BATCH_SIZE}, locale)));
        }

        Language resolvedLang = resolveLanguage(lang, acceptLanguage);
        log.debug("CMS Delivery: Fetching {} products, lang={}", uids.size(), resolvedLang);

        List<ProductDeliveryResponse> response = productCmsDeliveryService.getProductsByUids(uids, resolvedLang);
        return ResponseEntity.ok(ApiResponse.success(
                messageSource.getMessage("cms.products.found", null, locale), response));
    }

    @GetMapping("/category/{categoryUid}")
    @Operation(summary = "Get products by category", description = "Retrieves published products in a specific category")
    public ResponseEntity<ApiResponse<Page<ProductListDeliveryResponse>>> getProductsByCategory(
            @PathVariable String categoryUid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Language lang,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

        Locale locale = Locale.forLanguageTag(acceptLanguage);
        ResponseEntity<ApiResponse<Page<ProductListDeliveryResponse>>> rateLimitResponse = checkRateLimit(locale);
        if (rateLimitResponse != null) return rateLimitResponse;

        Language resolvedLang = resolveLanguage(lang, acceptLanguage);
        log.debug("CMS Delivery: Fetching products by category uid={}, lang={}", categoryUid, resolvedLang);

        Page<ProductListDeliveryResponse> response = productCmsDeliveryService
                .getProductsByCategory(categoryUid, resolvedLang, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                messageSource.getMessage("cms.products.found", null, locale), response));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Searches for published products by name or description")
    public ResponseEntity<ApiResponse<Page<ProductListDeliveryResponse>>> searchProducts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Language lang,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

        Locale locale = Locale.forLanguageTag(acceptLanguage);
        ResponseEntity<ApiResponse<Page<ProductListDeliveryResponse>>> rateLimitResponse = checkRateLimit(locale);
        if (rateLimitResponse != null) return rateLimitResponse;

        Language resolvedLang = resolveLanguage(lang, acceptLanguage);
        log.debug("CMS Delivery: Searching products query={}, lang={}", q, resolvedLang);

        Page<ProductListDeliveryResponse> response = productCmsDeliveryService
                .searchProducts(q, resolvedLang, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                messageSource.getMessage("cms.products.found", null, locale), response));
    }

    @GetMapping("/categories/{uid}")
    @Operation(summary = "Get category by UID", description = "Retrieves a visible category by its UID")
    public ResponseEntity<ApiResponse<CategoryDeliveryResponse>> getCategoryByUid(
            @PathVariable String uid,
            @RequestParam(required = false) Language lang,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

        Locale locale = Locale.forLanguageTag(acceptLanguage);
        ResponseEntity<ApiResponse<CategoryDeliveryResponse>> rateLimitResponse = checkRateLimit(locale);
        if (rateLimitResponse != null) return rateLimitResponse;

        Language resolvedLang = resolveLanguage(lang, acceptLanguage);
        log.debug("CMS Delivery: Fetching category uid={}, lang={}", uid, resolvedLang);

        return productCmsDeliveryService.getCategoryByUid(uid, resolvedLang)
                .map(response -> ResponseEntity.ok(
                        ApiResponse.success(messageSource.getMessage("cms.category.found", null, locale), response)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.error(messageSource.getMessage("cms.category.not.found", null, locale))));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get category tree", description = "Retrieves the full visible category tree")
    public ResponseEntity<ApiResponse<List<CategoryDeliveryResponse>>> getCategoryTree(
            @RequestParam(required = false) Language lang,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

        Locale locale = Locale.forLanguageTag(acceptLanguage);
        ResponseEntity<ApiResponse<List<CategoryDeliveryResponse>>> rateLimitResponse = checkRateLimit(locale);
        if (rateLimitResponse != null) return rateLimitResponse;

        Language resolvedLang = resolveLanguage(lang, acceptLanguage);
        log.debug("CMS Delivery: Fetching category tree, lang={}", resolvedLang);

        List<CategoryDeliveryResponse> response = productCmsDeliveryService.getCategoryTree(resolvedLang);
        return ResponseEntity.ok(ApiResponse.success(
                messageSource.getMessage("cms.categories.found", null, locale), response));
    }

    private <T> ResponseEntity<ApiResponse<T>> checkRateLimit(Locale locale) {
        String tenantId = tenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(messageSource.getMessage("cms.tenant.required", null, locale)));
        }

        RateLimiter limiter = rateLimiters.computeIfAbsent(tenantId,
                k -> RateLimiter.create(PERMITS_PER_SECOND));

        if (!limiter.tryAcquire()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                    ApiResponse.error(messageSource.getMessage("cms.rate.limit.exceeded", null, locale)));
        }

        return null;
    }

    private Language resolveLanguage(Language lang, String acceptLanguage) {
        if (lang != null) return lang;
        try {
            return Language.valueOf(acceptLanguage.toUpperCase());
        } catch (Exception e) {
            return Language.TR;
        }
    }
}
