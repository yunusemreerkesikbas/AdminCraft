package com.backend.application.service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.application.dto.delivery.NavigationDeliveryResponse;
import com.backend.domain.enums.Language;
import com.backend.application.dto.request.CreateEntryCompositeRequest;
import com.backend.application.dto.request.CreateEntryRequest;
import com.backend.application.dto.request.CreateNodeCompositeRequest;
import com.backend.application.dto.request.CreateNodeRequest;
import com.backend.application.dto.request.ReorderRequest;
import com.backend.application.dto.request.UpdateEntryCompositeRequest;
import com.backend.application.dto.request.UpdateEntryRequest;
import com.backend.application.dto.request.UpdateNodeCompositeRequest;
import com.backend.application.dto.request.UpdateNodeRequest;
import com.backend.application.dto.response.NavigationEntryCompositeResponse;
import com.backend.application.dto.response.NavigationEntryResponse;
import com.backend.application.dto.response.NavigationNodeCompositeResponse;
import com.backend.application.dto.response.NavigationNodeResponse;

public interface NavigationService {

  /**
   * Search root nodes with pagination support.
   * 
   * @param pageable    pagination parameters
   * @param searchQuery optional search query (min 2 chars)
   * @return paginated root nodes
   */
  Page<NavigationNodeResponse> searchRootNodes(Pageable pageable, String searchQuery);

  Optional<NavigationNodeResponse> getNodeById(Long id);

  NavigationNodeResponse createRootNode(CreateNodeRequest request);

  NavigationNodeResponse addChildNode(Long parentId, CreateNodeRequest request);

  NavigationNodeResponse updateNode(Long id, UpdateNodeRequest request);

  void deleteNode(Long id);

  void reorderChildren(Long parentId, ReorderRequest<Long> request);

  NavigationEntryResponse createEntry(CreateEntryRequest request);

  NavigationEntryResponse updateEntry(Long id, UpdateEntryRequest request);

  void deleteEntry(Long id);

  void reorderEntries(Long nodeId, ReorderRequest<Long> request);

  NavigationNodeCompositeResponse createNodeComposite(CreateNodeCompositeRequest request);

  Optional<NavigationNodeCompositeResponse> getNodeComposite(Long id);

  NavigationNodeCompositeResponse updateNodeComposite(Long id, UpdateNodeCompositeRequest request);

  NavigationEntryCompositeResponse createEntryComposite(CreateEntryCompositeRequest request);

  Optional<NavigationEntryCompositeResponse> getEntryComposite(Long id);

  NavigationEntryCompositeResponse updateEntryComposite(Long id, UpdateEntryCompositeRequest request);

  Optional<NavigationDeliveryResponse> getNavigationById(Long id, Language lang);

  Map<Long, NavigationDeliveryResponse> getNavigationsByIds(Set<Long> ids, Language lang);

  Optional<NavigationDeliveryResponse> getNavigationByUid(String uid, Language lang);
}
