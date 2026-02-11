package com.backend.application.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.request.PatchPlatformSettingsRequest;
import com.backend.application.dto.response.PlatformSettingsData;
import com.backend.domain.enums.TwoFactorPolicy;
import com.backend.domain.port.EncryptionServicePort;
import com.backend.domain.port.PlatformSettingsPort;
import com.backend.infrastructure.persistence.platform.entity.PlatformSettings;
import com.backend.infrastructure.persistence.platform.repository.PlatformSettingsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlatformSettingsServiceImpl implements PlatformSettingsService {

    private final PlatformSettingsPort platformSettingsPort;
    private final PlatformSettingsRepository platformSettingsRepository;
    private final EncryptionServicePort encryptionService;

    @Override
    public PlatformSettingsData getSettings() {
        PlatformSettings entity = platformSettingsPort.getSingleton();
        return toData(entity);
    }

    @Override
    @Transactional
    public PlatformSettingsData patchSettings(PatchPlatformSettingsRequest request) {
        PlatformSettings entity = platformSettingsPort.getSingleton();

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
        if (request.twoFactorPolicy() != null) {
            entity.setTwoFactorPolicy(request.twoFactorPolicy());
        }
        if (request.recaptchaEnabled() != null) {
            entity.setRecaptchaEnabled(request.recaptchaEnabled());
        }
        if (request.recaptchaSiteKey() != null) {
            entity.setRecaptchaSiteKey(request.recaptchaSiteKey());
        }
        if (request.recaptchaSecretKey() != null && !request.recaptchaSecretKey().isBlank()) {
            entity.setRecaptchaSecretKeyEncrypted(encryptionService.encrypt(request.recaptchaSecretKey()));
        }
        if (request.recaptchaThreshold() != null) {
            entity.setRecaptchaThreshold(request.recaptchaThreshold());
        }

        validateSecurityConfiguration(entity);

        PlatformSettings saved = platformSettingsRepository.save(entity);
        return toData(saved);
    }

    private void validateSecurityConfiguration(PlatformSettings entity) {
        if (!Boolean.TRUE.equals(entity.getRecaptchaEnabled())) {
            if (entity.getTwoFactorPolicy() == null) {
                entity.setTwoFactorPolicy(TwoFactorPolicy.DISABLED);
            }
            if (entity.getRecaptchaThreshold() == null) {
                entity.setRecaptchaThreshold(new BigDecimal("0.5"));
            }
            return;
        }

        // Bean Validation handles reCAPTCHA key validation (@RecaptchaKeysValid annotation)

        if (entity.getTwoFactorPolicy() == null) {
            entity.setTwoFactorPolicy(TwoFactorPolicy.DISABLED);
        }
        if (entity.getRecaptchaThreshold() == null) {
            entity.setRecaptchaThreshold(new BigDecimal("0.5"));
        }
    }

    private PlatformSettingsData toData(PlatformSettings entity) {
        return new PlatformSettingsData(
                entity.getPlatformName(),
                entity.getDefaultLanguage(),
                entity.getDefaultCurrency(),
                entity.getEmailFromAddress(),
                entity.getEmailFromName(),
                entity.getTwoFactorPolicy() != null ? entity.getTwoFactorPolicy() : TwoFactorPolicy.DISABLED,
                entity.getRecaptchaEnabled() != null ? entity.getRecaptchaEnabled() : false,
                entity.getRecaptchaSiteKey(),
                entity.getRecaptchaThreshold() != null ? entity.getRecaptchaThreshold() : new BigDecimal("0.5"));
    }
}
