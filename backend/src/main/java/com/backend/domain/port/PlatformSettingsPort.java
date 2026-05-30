package com.backend.domain.port;

import com.backend.infrastructure.persistence.platform.entity.PlatformSettings;
import com.backend.domain.enums.TwoFactorPolicy;

/**
 * Domain port for platform settings access.
 * Allows Application layer to interact with platform settings without depending on Infrastructure.
 */
public interface PlatformSettingsPort {
    /**
     * Get the singleton platform settings record.
     * @return PlatformSettings entity
     */
    PlatformSettings getSingleton();

    default TwoFactorPolicy getTwoFactorPolicy() {
        return getSingleton().getTwoFactorPolicy();
    }

    default String getDefaultLanguage() {
        return getSingleton().getDefaultLanguage();
    }

    PlatformSettings updateTwoFactorPolicy(TwoFactorPolicy policy);
}
