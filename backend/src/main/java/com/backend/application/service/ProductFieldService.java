package com.backend.application.service;

import java.util.List;
import java.util.Map;

import com.backend.application.dto.request.CreateProductFieldRequest;
import com.backend.application.dto.request.UpdateProductFieldRequest;
import com.backend.application.dto.response.ProductFieldDefinitionResponse;
import com.backend.domain.entity.Product;

public interface ProductFieldService {

  List<ProductFieldDefinitionResponse> findAllDefinitions();

  List<ProductFieldDefinitionResponse> findVisibleDefinitions();

  ProductFieldDefinitionResponse findDefinitionById(Long id);

  ProductFieldDefinitionResponse createDefinition(CreateProductFieldRequest request);

  ProductFieldDefinitionResponse updateDefinition(Long id, UpdateProductFieldRequest request);

  void deleteDefinition(Long id);

  void saveFieldValues(Product product, Map<String, Object> fieldValues);

  Map<String, Object> getFieldValues(Long productId);

  void deleteFieldValues(Long productId);
}
