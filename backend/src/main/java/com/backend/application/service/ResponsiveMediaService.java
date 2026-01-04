package com.backend.application.service;

import java.util.List;

import com.backend.application.dto.delivery.ResponsiveMediaDeliveryResponse;
import com.backend.application.dto.request.ResponsiveMediaRequest;
import com.backend.application.dto.response.ResponsiveMediaResponse;
import com.backend.domain.entity.ResponsiveMediaSet;
import com.backend.domain.enums.Language;

/**
 * Service interface for ResponsiveMediaSet operations.
 */
public interface ResponsiveMediaService {

  /**
   * Create a new responsive media set.
   */
  ResponsiveMediaResponse create(ResponsiveMediaRequest request);

  /**
   * Get responsive media set by ID.
   */
  ResponsiveMediaResponse getById(Long id);

  /**
   * Get responsive media set by UID.
   */
  ResponsiveMediaResponse getByUid(String uid);

  /**
   * Update an existing responsive media set.
   */
  ResponsiveMediaResponse update(Long id, ResponsiveMediaRequest request);

  /**
   * Delete a responsive media set.
   */
  void delete(Long id);

  /**
   * Convert entity to delivery response for CMS.
   */
  ResponsiveMediaDeliveryResponse toDeliveryResponse(ResponsiveMediaSet entity, Language language);

  /**
   * Get component IDs that use a specific media.
   */
  List<Long> getLinkedComponentIds(Long mediaId);

  /**
   * Get entity by ID (for internal use).
   */
  ResponsiveMediaSet getEntityById(Long id);
}
