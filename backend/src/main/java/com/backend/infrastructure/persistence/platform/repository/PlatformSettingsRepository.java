package com.backend.infrastructure.persistence.platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.domain.port.PlatformSettingsPort;
import com.backend.domain.enums.TwoFactorPolicy;
import com.backend.infrastructure.persistence.platform.entity.PlatformSettings;

@Repository
public interface PlatformSettingsRepository extends JpaRepository<PlatformSettings, Long>, PlatformSettingsPort {

    default PlatformSettings getSingleton() {
        return findById(1L).orElseThrow(() -> new IllegalStateException("Platform settings not initialized"));
    }

    default PlatformSettings updateTwoFactorPolicy(TwoFactorPolicy policy) {
        PlatformSettings settings = getSingleton();
        settings.setTwoFactorPolicy(policy);
        return save(settings);
    }
}
