package com.backend.infrastructure.persistence.platform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.infrastructure.persistence.platform.entity.PlatformConfigProperty;

interface JpaPlatformConfigPropertyRepository extends JpaRepository<PlatformConfigProperty, Long> {

    Optional<PlatformConfigProperty> findByConfigKey(String configKey);

    @Modifying
    @Query("DELETE FROM PlatformConfigProperty p WHERE p.configKey = :configKey")
    void deleteByConfigKey(@Param("configKey") String configKey);
}
