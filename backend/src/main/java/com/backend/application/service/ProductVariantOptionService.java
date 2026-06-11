package com.backend.application.service;

import java.util.List;

import com.backend.application.dto.request.CreateProductVariantOptionRequest;
import com.backend.application.dto.request.UpdateProductVariantOptionRequest;
import com.backend.application.dto.response.ProductVariantOptionResponse;

public interface ProductVariantOptionService {

    List<ProductVariantOptionResponse> findAll();

    ProductVariantOptionResponse findById(Long id);

    ProductVariantOptionResponse create(CreateProductVariantOptionRequest request);

    ProductVariantOptionResponse update(Long id, UpdateProductVariantOptionRequest request);

    void delete(Long id);
}
