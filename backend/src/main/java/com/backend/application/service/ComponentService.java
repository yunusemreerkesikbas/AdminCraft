package com.backend.application.service;

import com.backend.domain.enums.ComponentType;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.ComponentListFilter;
import com.backend.presentation.dto.request.ComponentRequest;
import com.backend.presentation.dto.response.ComponentResponse;
import com.backend.presentation.dto.response.SiteComponentResponse;

import java.util.List;

public interface ComponentService {
  ComponentResponse create(Long tenantId, ComponentRequest request);

  ComponentResponse update(Long id, Long tenantId, ComponentRequest request);

  void delete(Long id, Long tenantId);

  ComponentResponse get(Long id, Long tenantId);

  /**
   * NAVBAR için birleşik detay: root + flat items[].
   */
  ComponentResponse getNavbarDetail(Long id, Long tenantId);

  List<ComponentResponse> list(Long tenantId);

  List<ComponentResponse> list(Long tenantId, ComponentListFilter filter);

  List<SiteComponentResponse> getSiteComponents(Long tenantId, ComponentType type, Language language);
}