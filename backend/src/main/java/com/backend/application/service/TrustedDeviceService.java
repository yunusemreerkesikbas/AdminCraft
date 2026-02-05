package com.backend.application.service;

import java.util.List;

import com.backend.domain.entity.TrustedDevice;
import com.backend.domain.entity.User;

public interface TrustedDeviceService {

    boolean isDeviceTrusted(Long userId, String deviceFingerprint);

    TrustedDevice addTrustedDevice(User user, String deviceFingerprint, DeviceInfo deviceInfo);

    void removeTrustedDevice(Long userId, String deviceFingerprint);

    void removeAllTrustedDevices(Long userId);

    void updateLastUsed(Long userId, String deviceFingerprint);

    List<TrustedDevice> getUserDevices(Long userId);

    List<TrustedDevice> getValidUserDevices(Long userId);

    void cleanupExpiredDevices();

    record DeviceInfo(
            String deviceName,
            String browser,
            String os,
            String ipAddress
    ) {}
}
