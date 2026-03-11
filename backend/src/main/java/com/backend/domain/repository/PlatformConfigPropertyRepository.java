package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.PlatformConfigProperty;

public interface PlatformConfigPropertyRepository {

    PlatformConfigProperty save(PlatformConfigProperty entity);

    Optional<PlatformConfigProperty> findByConfigKey(String configKey);

    List<PlatformConfigProperty> findAll();

    void deleteByConfigKey(String configKey);
}

