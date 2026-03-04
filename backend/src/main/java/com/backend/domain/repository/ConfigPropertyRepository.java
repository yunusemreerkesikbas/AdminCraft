package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.ConfigProperty;

public interface ConfigPropertyRepository {

    ConfigProperty save(ConfigProperty entity);

    Optional<ConfigProperty> findByConfigKey(String configKey);

    List<ConfigProperty> findAll();

    void deleteByConfigKey(String configKey);
}

