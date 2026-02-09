package com.backend.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.request.PatchPlatformSettingsRequest;
import com.backend.infrastructure.persistence.platform.entity.PlatformSettings;
import com.backend.infrastructure.persistence.platform.repository.PlatformSettingsRepository;
import com.backend.presentation.dto.response.PlatformSettingsResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PlatformSettingsServiceImpl implements PlatformSettingsService {

    private final PlatformSettingsRepository platformSettingsRepository;

    @Override
    public PlatformSettingsResponse getSettings() {
        PlatformSettings entity = platformSettingsRepository.getSingleton();
        return PlatformSettingsResponse.from(entity);
    }

    @Override
    @Transactional
    public PlatformSettingsResponse patchSettings(PatchPlatformSettingsRequest request) {
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
        return PlatformSettingsResponse.from(saved);
    }
}
