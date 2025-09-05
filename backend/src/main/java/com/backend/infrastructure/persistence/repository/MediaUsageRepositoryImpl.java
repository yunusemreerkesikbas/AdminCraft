package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.MediaUsage;
import com.backend.domain.enums.MediaPurpose;
import com.backend.domain.repository.MediaUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MediaUsageRepositoryImpl implements MediaUsageRepository {

  private final MediaUsageJpaRepository jpa;

  @Override
  public MediaUsage save(MediaUsage usage) {
    return jpa.save(usage);
  }

  @Override
  public List<MediaUsage> saveAll(Iterable<MediaUsage> usages) {
    return jpa.saveAll(usages);
  }

  @Override
  public Optional<MediaUsage> findById(Long id) {
    return jpa.findById(id);
  }

  @Override
  public void deleteById(Long id) {
    jpa.deleteById(id);
  }

  @Override
  public List<MediaUsage> findByTenantIdAndOwner(String ownerType, Long tenantId, Long ownerId) {
    return jpa.findByTenantAndOwner(tenantId, ownerType, ownerId);
  }

  @Override
  public List<MediaUsage> findByTenantIdAndOwnerOrdered(String ownerType, Long tenantId, Long ownerId) {
    return jpa.findByTenantAndOwnerOrdered(tenantId, ownerType, ownerId);
  }

  @Override
  public Optional<MediaUsage> findCoverByTenantIdAndOwner(String ownerType, Long tenantId, Long ownerId) {
    return jpa.findCoverByTenantAndOwner(tenantId, ownerType, ownerId);
  }

  @Override
  public List<MediaUsage> findByTenantIdAndOwnerAndPurpose(String ownerType, Long tenantId, Long ownerId,
      MediaPurpose purpose) {
    return jpa.findByTenantAndOwnerAndPurpose(tenantId, ownerType, ownerId, purpose);
  }

  @Override
  public void deleteByTenantIdAndOwner(String ownerType, Long tenantId, Long ownerId) {
    jpa.deleteByTenantIdAndOwnerTypeAndOwnerId(tenantId, ownerType, ownerId);
  }
}
