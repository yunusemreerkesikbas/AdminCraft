package com.backend.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.TrustedDevice;
import com.backend.domain.repository.TrustedDeviceRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TrustedDeviceRepositoryImpl implements TrustedDeviceRepository {

    private final TrustedDeviceJpaRepository jpaRepository;

    @Override
    public TrustedDevice save(TrustedDevice device) {
        return jpaRepository.save(device);
    }

    @Override
    public Optional<TrustedDevice> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Optional<TrustedDevice> findByUserIdAndDeviceFingerprint(Long userId, String deviceFingerprint) {
        return jpaRepository.findByUserIdAndDeviceFingerprint(userId, deviceFingerprint);
    }

    @Override
    public List<TrustedDevice> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public List<TrustedDevice> findByUserIdAndExpiresAtAfter(Long userId, LocalDateTime now) {
        return jpaRepository.findByUserIdAndExpiresAtAfter(userId, now);
    }

    @Override
    public boolean existsByUserIdAndDeviceFingerprint(Long userId, String deviceFingerprint) {
        return jpaRepository.existsByUserIdAndDeviceFingerprint(userId, deviceFingerprint);
    }

    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        jpaRepository.deleteByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteByUserIdAndDeviceFingerprint(Long userId, String deviceFingerprint) {
        jpaRepository.deleteByUserIdAndDeviceFingerprint(userId, deviceFingerprint);
    }

    @Override
    @Transactional
    public void deleteExpiredDevices(LocalDateTime before) {
        jpaRepository.deleteExpiredDevices(before);
    }

    @Override
    public long countByUserId(Long userId) {
        return jpaRepository.countByUserId(userId);
    }
}
