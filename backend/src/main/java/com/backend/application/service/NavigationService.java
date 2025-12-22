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

  /**
 * Retrieves the root navigation nodes.
 *
 * @return the list of root NavigationNodeResponse objects; an empty list if no root nodes exist
 */
List<NavigationNodeResponse> getRootNodes();

  /**
 * Retrieves a navigation node by its identifier.
 *
 * @param id the identifier of the navigation node to retrieve
 * @return an Optional containing the node response if found, otherwise empty
 */
Optional<NavigationNodeResponse> getNodeById(Long id);

  NavigationNodeResponse createRootNode(CreateNodeRequest request);

  NavigationNodeResponse addChildNode(Long parentId, CreateNodeRequest request);

  NavigationNodeResponse updateNode(Long id, UpdateNodeRequest request);

  void deleteNode(Long id);

  /**
 * Reorders the child nodes of the specified parent according to the provided reorder request.
 *
 * @param parentId the identifier of the parent node whose children will be reordered
 * @param request  the reorder request describing the desired ordering of child node IDs
 */
void reorderChildren(Long parentId, ReorderRequest<Long> request);

  /**
 * Creates a new navigation entry from the provided request data.
 *
 * @param request the DTO containing data for the new navigation entry
 * @return the created NavigationEntryResponse representing the new navigation entry
 */
NavigationEntryResponse createEntry(CreateEntryRequest request);

  /**
 * Update an existing navigation entry identified by its id with the provided changes.
 *
 * @param id      the identifier of the navigation entry to update
 * @param request the update data for the navigation entry
 * @return the updated navigation entry
 */
NavigationEntryResponse updateEntry(Long id, UpdateEntryRequest request);

  void deleteEntry(Long id);

  /**
 * Reorders the entries belonging to the specified navigation node according to the provided reorder request.
 *
 * @param nodeId the ID of the navigation node whose entries will be reordered
 * @param request a ReorderRequest containing the desired ordering of entry IDs
 */
void reorderEntries(Long nodeId, ReorderRequest<Long> request);

  /**
 * Creates a composite navigation node using the provided request data.
 *
 * @param request payload containing the node data and any related child nodes or entries to create
 * @return the created composite navigation node, including the persisted node and its related children/entries
 */
NavigationNodeCompositeResponse createNodeComposite(CreateNodeCompositeRequest request);

  /**
 * Update a composite navigation node identified by its ID and return the updated composite representation.
 *
 * @param id the identifier of the navigation node to update
 * @param request the composite update data for the node (may include related entries and settings)
 * @return the updated NavigationNodeCompositeResponse representing the node and its related data
 */
NavigationNodeCompositeResponse updateNodeComposite(Long id, UpdateNodeCompositeRequest request);

  /**
 * Creates a composite navigation entry that includes the entry's data together with related sub-resources.
 *
 * @param request payload containing the navigation entry fields and any related composite data to create
 * @return the created composite navigation entry with populated related resources
 */
NavigationEntryCompositeResponse createEntryComposite(CreateEntryCompositeRequest request);

  /**
 * Update a navigation entry and its related composite data using the provided request.
 *
 * @param id      the identifier of the navigation entry to update
 * @param request the composite update payload containing entry and related data
 * @return the updated composite navigation entry response
 */
NavigationEntryCompositeResponse updateEntryComposite(Long id, UpdateEntryCompositeRequest request);

  /**
 * Retrieve navigation delivery data by its UID.
 *
 * @param uid the unique identifier of the navigation to look up
 * @return an Optional containing the delivery response for the navigation with the given UID, or empty if no matching navigation exists
 */
Optional<NavigationDeliveryResponse> getNavigationByUid(String uid);
}