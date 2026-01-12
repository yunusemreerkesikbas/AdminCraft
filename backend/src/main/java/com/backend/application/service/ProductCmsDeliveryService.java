package com.backend.application.service;

import com.backend.application.dto.delivery.CategoryDeliveryResponse;
import com.backend.application.dto.delivery.ProductDeliveryResponse;
import com.backend.application.dto.delivery.ProductListDeliveryResponse;
import com.backend.domain.enums.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductCmsDeliveryService {

    Optional<ProductDeliveryResponse> getProductByUid(String uid, Language language);

    List<ProductDeliveryResponse> getProductsByUids(List<String> uids, Language language);

    Page<ProductListDeliveryResponse> getProductsByCategory(String categoryUid, Language language, Pageable pageable);

    Page<ProductListDeliveryResponse> searchProducts(String query, Language language, Pageable pageable);

    Optional<CategoryDeliveryResponse> getCategoryByUid(String uid, Language language);

    List<CategoryDeliveryResponse> getCategoryTree(Language language);
}
