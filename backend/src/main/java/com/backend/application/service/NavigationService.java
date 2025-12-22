package com.backend.application.service;

import java.util.List;
import java.util.Optional;

import com.backend.application.dto.delivery.NavigationDeliveryResponse;
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

  List<NavigationNodeResponse> getRootNodes();

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

  NavigationNodeCompositeResponse updateNodeComposite(Long id, UpdateNodeCompositeRequest request);

  NavigationEntryCompositeResponse createEntryComposite(CreateEntryCompositeRequest request);

  NavigationEntryCompositeResponse updateEntryComposite(Long id, UpdateEntryCompositeRequest request);

  Optional<NavigationDeliveryResponse> getNavigationByUid(String uid);
}
