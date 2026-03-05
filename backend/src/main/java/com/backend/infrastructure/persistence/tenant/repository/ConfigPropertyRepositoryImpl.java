package com.backend.infrastructure.persistence.tenant.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.backend.domain.entity.ConfigProperty;
import com.backend.domain.repository.ConfigPropertyRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ConfigPropertyRepositoryImpl implements ConfigPropertyRepository {

    private final JpaConfigPropertyRepository jpaRepository;

    @Override
    public ConfigProperty save(ConfigProperty entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<ConfigProperty> findByConfigKey(String configKey) {
        return jpaRepository.findByConfigKey(configKey);
    }

    @Override
    public List<ConfigProperty> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteByConfigKey(String configKey) {
        jpaRepository.deleteByConfigKey(configKey);
    }
}
