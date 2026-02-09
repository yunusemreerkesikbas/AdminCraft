package com.backend.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.request.PatchPlatformSettingsRequest;
import com.backend.application.dto.response.PlatformSettingsData;
import com.backend.infrastructure.persistence.platform.entity.PlatformSettings;
import com.backend.infrastructure.persistence.platform.repository.PlatformSettingsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlatformSettingsServiceImpl implements PlatformSettingsService {

    private final PlatformSettingsRepository platformSettingsRepository;

    @Override
    public PlatformSettingsData getSettings() {
        PlatformSettings entity = platformSettingsRepository.getSingleton();
        return toData(entity);
    }

    @Override
    @Transactional
    public PlatformSettingsData patchSettings(PatchPlatformSettingsRequest request) {
        PlatformSettings entity = platformSettingsRepository.getSingleton();

        if (request.platformName() != null) {
            entity.setPlatformName(request.platformName());
        }
        if (request.defaultLanguage() != null) {
            entity.setDefaultLanguage(request.defaultLanguage());
        }
        if (request.defaultCurrency() != null) {
            entity.setDefaultCurrency(request.defaultCurrency());
        }
        if (request.emailFromAddress() != null) {
            entity.setEmailFromAddress(request.emailFromAddress());
        }
        if (request.emailFromName() != null) {
            entity.setEmailFromName(request.emailFromName());
        }

        PlatformSettings saved = platformSettingsRepository.save(entity);
        return toData(saved);
    }

    private PlatformSettingsData toData(PlatformSettings entity) {
        return new PlatformSettingsData(
                entity.getPlatformName(),
                entity.getDefaultLanguage(),
                entity.getDefaultCurrency(),
                entity.getEmailFromAddress(),
                entity.getEmailFromName());
    }
}
