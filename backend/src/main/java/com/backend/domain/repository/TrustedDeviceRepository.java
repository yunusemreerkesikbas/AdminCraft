package com.backend.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.TrustedDevice;

public interface TrustedDeviceRepository {

    TrustedDevice save(TrustedDevice device);

    Optional<TrustedDevice> findById(Long id);

    void deleteById(Long id);

    Optional<TrustedDevice> findByUserIdAndDeviceFingerprint(Long userId, String deviceFingerprint);

    List<TrustedDevice> findByUserId(Long userId);

    List<TrustedDevice> findByUserIdAndExpiresAtAfter(Long userId, LocalDateTime now);

    boolean existsByUserIdAndDeviceFingerprint(Long userId, String deviceFingerprint);

    void deleteByUserId(Long userId);

    void deleteByUserIdAndDeviceFingerprint(Long userId, String deviceFingerprint);

    void deleteExpiredDevices(LocalDateTime before);

    long countByUserId(Long userId);
}
