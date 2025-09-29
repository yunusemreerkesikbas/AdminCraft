package com.backend.application.service;

import com.backend.presentation.dto.request.NavbarItemRequest;
import com.backend.presentation.dto.request.NavbarItemsReorderRequest;
import com.backend.presentation.dto.response.NavbarItemResponse;

import java.util.List;

public interface ComponentItemService {
  // İç kullanım: create/update sonrası tek item'i zengin döndürmek için ağaç
  // yapıdan faydalanıyoruz. Controller'da /items/tree kaldırıldı.
  java.util.List<com.backend.presentation.dto.response.NavbarItemResponse> listTree(Long tenantId, Long componentId);

  NavbarItemResponse create(Long tenantId, Long componentId, NavbarItemRequest request);

  NavbarItemResponse update(Long tenantId, Long componentId, Long itemId, NavbarItemRequest request);

  void delete(Long tenantId, Long componentId, Long itemId);

  void reorder(Long tenantId, Long componentId, NavbarItemsReorderRequest request);
}
