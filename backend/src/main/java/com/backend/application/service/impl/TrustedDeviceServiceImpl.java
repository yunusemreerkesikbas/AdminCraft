package com.backend.application.service.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.service.TrustedDeviceService;
import com.backend.domain.entity.TrustedDevice;
import com.backend.domain.entity.User;
import com.backend.domain.repository.TrustedDeviceRepository;
import com.backend.infrastructure.email.TrustedDeviceProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrustedDeviceServiceImpl implements TrustedDeviceService {

    private final TrustedDeviceRepository trustedDeviceRepository;
    private final TrustedDeviceProperties trustedDeviceProperties;

    @Override
    @Transactional("tenantTransactionManager")
    public boolean isDeviceTrusted(Long userId, String deviceFingerprint) {
        if (deviceFingerprint == null || deviceFingerprint.isBlank()) {
            return false;
        }

        Optional<TrustedDevice> device = trustedDeviceRepository
                .findByUserIdAndDeviceFingerprint(userId, deviceFingerprint);

        if (device.isEmpty()) {
            return false;
        }

        TrustedDevice trustedDevice = device.get();
        if (trustedDevice.isExpired()) {
            log.info("Trusted device expired for user: {}", userId);
            trustedDeviceRepository.deleteById(trustedDevice.getId());
            return false;
        }

        trustedDevice.updateLastUsed();
        trustedDeviceRepository.save(trustedDevice);
        return true;
    }

    @Override
    @Transactional("tenantTransactionManager")
    public TrustedDevice addTrustedDevice(User user, String deviceFingerprint, DeviceInfo deviceInfo) {
        Optional<TrustedDevice> existing = trustedDeviceRepository
                .findByUserIdAndDeviceFingerprint(user.getId(), deviceFingerprint);

        if (existing.isPresent()) {
            TrustedDevice device = existing.get();
            device.extendExpiry(trustedDeviceProperties.getExpiryDays());
            device.updateLastUsed();
            log.info("Extended trust for existing device: userId={}", user.getId());
            return trustedDeviceRepository.save(device);
        }

        long deviceCount = trustedDeviceRepository.countByUserId(user.getId());
        if (deviceCount >= trustedDeviceProperties.getMaxDevicesPerUser()) {
            log.warn("Max trusted devices reached for user: {}. Removing oldest.", user.getId());
            removeOldestDevice(user.getId());
        }

        TrustedDevice newDevice = TrustedDevice.builder()
                .user(user)
                .deviceFingerprint(deviceFingerprint)
                .deviceName(deviceInfo.deviceName())
                .browser(deviceInfo.browser())
                .os(deviceInfo.os())
                .ipAddress(deviceInfo.ipAddress())
                .expiresAt(LocalDateTime.now().plusDays(trustedDeviceProperties.getExpiryDays()))
                .build();

        log.info("Added new trusted device for user: {}", user.getId());
        return trustedDeviceRepository.save(newDevice);
    }

    @Override
    @Transactional("tenantTransactionManager")
    public void removeTrustedDevice(Long userId, String deviceFingerprint) {
        trustedDeviceRepository.deleteByUserIdAndDeviceFingerprint(userId, deviceFingerprint);
        log.info("Removed trusted device for user: {}", userId);
    }

    @Override
    @Transactional("tenantTransactionManager")
    public void removeAllTrustedDevices(Long userId) {
        trustedDeviceRepository.deleteByUserId(userId);
        log.info("Removed all trusted devices for user: {}", userId);
    }

    @Override
    @Transactional("tenantTransactionManager")
    public void updateLastUsed(Long userId, String deviceFingerprint) {
        trustedDeviceRepository.findByUserIdAndDeviceFingerprint(userId, deviceFingerprint)
                .ifPresent(device -> {
                    device.updateLastUsed();
                    trustedDeviceRepository.save(device);
                });
    }

    @Override
    @Transactional(value = "tenantTransactionManager", readOnly = true)
    public List<TrustedDevice> getUserDevices(Long userId) {
        return trustedDeviceRepository.findByUserId(userId);
    }

    @Override
    @Transactional(value = "tenantTransactionManager", readOnly = true)
    public List<TrustedDevice> getValidUserDevices(Long userId) {
        return trustedDeviceRepository.findByUserIdAndExpiresAtAfter(userId, LocalDateTime.now());
    }

    @Override
    @Transactional("tenantTransactionManager")
    public void cleanupExpiredDevices() {
        LocalDateTime cutoff = LocalDateTime.now();
        trustedDeviceRepository.deleteExpiredDevices(cutoff);
        log.info("Cleaned up expired trusted devices");
    }

    private void removeOldestDevice(Long userId) {
        List<TrustedDevice> devices = trustedDeviceRepository.findByUserId(userId);
        if (!devices.isEmpty()) {
            TrustedDevice oldest = devices.stream()
                    .min(Comparator.comparing(TrustedDevice::getLastUsedAt))
                    .orElse(devices.get(0));
            trustedDeviceRepository.deleteById(oldest.getId());
        }
    }
}
