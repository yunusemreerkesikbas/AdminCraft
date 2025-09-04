package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.MediaUsage;
import com.backend.domain.enums.MediaPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaUsageJpaRepository extends JpaRepository<MediaUsage, Long> {

  @Query("SELECT u FROM MediaUsage u WHERE u.tenantId = :tenantId AND u.ownerType = :ownerType AND u.ownerId = :ownerId")
  List<MediaUsage> findByTenantAndOwner(@Param("tenantId") Long tenantId,
      @Param("ownerType") String ownerType,
      @Param("ownerId") Long ownerId);

  @Query("SELECT u FROM MediaUsage u WHERE u.tenantId = :tenantId AND u.ownerType = :ownerType AND u.ownerId = :ownerId ORDER BY u.sortOrder ASC, u.id ASC")
  List<MediaUsage> findByTenantAndOwnerOrdered(@Param("tenantId") Long tenantId,
      @Param("ownerType") String ownerType,
      @Param("ownerId") Long ownerId);

  @Query("SELECT u FROM MediaUsage u WHERE u.tenantId = :tenantId AND u.ownerType = :ownerType AND u.ownerId = :ownerId AND u.isCover = true")
  Optional<MediaUsage> findCoverByTenantAndOwner(@Param("tenantId") Long tenantId,
      @Param("ownerType") String ownerType,
      @Param("ownerId") Long ownerId);

  @Query("SELECT u FROM MediaUsage u WHERE u.tenantId = :tenantId AND u.ownerType = :ownerType AND u.ownerId = :ownerId AND u.purpose = :purpose")
  List<MediaUsage> findByTenantAndOwnerAndPurpose(@Param("tenantId") Long tenantId,
      @Param("ownerType") String ownerType,
      @Param("ownerId") Long ownerId,
      @Param("purpose") MediaPurpose purpose);

  void deleteByTenantIdAndOwnerTypeAndOwnerId(Long tenantId, String ownerType, Long ownerId);
}
