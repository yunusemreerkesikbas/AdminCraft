package com.backend.domain.repository;

import com.backend.domain.entity.MediaUsage;
import com.backend.domain.enums.MediaPurpose;
import java.util.List;
import java.util.Optional;

public interface MediaUsageRepository {

  MediaUsage save(MediaUsage usage);

  List<MediaUsage> saveAll(Iterable<MediaUsage> usages);

  Optional<MediaUsage> findById(Long id);

  void deleteById(Long id);

  List<MediaUsage> findByTenantIdAndOwner(String ownerType, Long tenantId, Long ownerId);

  List<MediaUsage> findByTenantIdAndOwnerOrdered(String ownerType, Long tenantId, Long ownerId);

  Optional<MediaUsage> findCoverByTenantIdAndOwner(String ownerType, Long tenantId, Long ownerId);

  List<MediaUsage> findByTenantIdAndOwnerAndPurpose(String ownerType, Long tenantId, Long ownerId,
      MediaPurpose purpose);

  void deleteByTenantIdAndOwner(String ownerType, Long tenantId, Long ownerId);
}
