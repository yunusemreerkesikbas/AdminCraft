package com.backend.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.TrustedDevice;

@Repository
public interface TrustedDeviceJpaRepository extends JpaRepository<TrustedDevice, Long> {

    Optional<TrustedDevice> findByUserIdAndDeviceFingerprint(Long userId, String deviceFingerprint);

    List<TrustedDevice> findByUserId(Long userId);

    List<TrustedDevice> findByUserIdAndExpiresAtAfter(Long userId, LocalDateTime now);

    boolean existsByUserIdAndDeviceFingerprint(Long userId, String deviceFingerprint);

    @Modifying
    @Query("DELETE FROM TrustedDevice t WHERE t.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM TrustedDevice t WHERE t.user.id = :userId AND t.deviceFingerprint = :deviceFingerprint")
    void deleteByUserIdAndDeviceFingerprint(@Param("userId") Long userId, @Param("deviceFingerprint") String deviceFingerprint);

    @Modifying
    @Query("DELETE FROM TrustedDevice t WHERE t.expiresAt < :before")
    void deleteExpiredDevices(@Param("before") LocalDateTime before);

    long countByUserId(Long userId);
}
