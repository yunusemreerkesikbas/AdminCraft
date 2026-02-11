package com.backend.domain.port;

import com.backend.infrastructure.persistence.platform.entity.PlatformSettings;

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
}
