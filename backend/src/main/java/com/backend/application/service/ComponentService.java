package com.backend.application.service;

import com.backend.presentation.dto.request.CreateComponentRequest;
import com.backend.presentation.dto.request.UpdateComponentRequest;
import com.backend.presentation.dto.response.ComponentResponse;

import java.util.List;

public interface ComponentService {
  ComponentResponse create(CreateComponentRequest request, Long createdBy);

  ComponentResponse update(Long id, Long tenantId, UpdateComponentRequest request, Long updatedBy);

  void delete(Long id, Long tenantId);

  ComponentResponse get(Long id, Long tenantId);

  List<ComponentResponse> list(Long tenantId);
}
