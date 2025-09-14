package com.backend.application.service;

import com.backend.presentation.dto.request.ComponentListFilter;
import com.backend.presentation.dto.request.ComponentRequest;
import com.backend.presentation.dto.response.ComponentResponse;

import java.util.List;

public interface ComponentService {
  ComponentResponse create(Long tenantId, ComponentRequest request);

  ComponentResponse update(Long id, Long tenantId, ComponentRequest request);

  void delete(Long id, Long tenantId);

  ComponentResponse get(Long id, Long tenantId);

  List<ComponentResponse> list(Long tenantId);

  List<ComponentResponse> list(Long tenantId, ComponentListFilter filter);
}
