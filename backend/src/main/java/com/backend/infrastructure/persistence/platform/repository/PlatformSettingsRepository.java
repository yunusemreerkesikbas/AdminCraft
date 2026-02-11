package com.backend.infrastructure.persistence.platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.domain.port.PlatformSettingsPort;
import com.backend.infrastructure.persistence.platform.entity.PlatformSettings;

@Repository
public interface PlatformSettingsRepository extends JpaRepository<PlatformSettings, Long>, PlatformSettingsPort {

    default PlatformSettings getSingleton() {
        return findById(1L).orElseThrow(() -> new IllegalStateException("Platform settings not initialized"));
    }
}
