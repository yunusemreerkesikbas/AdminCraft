package com.backend.infrastructure.persistence.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.backend.domain.entity.PlatformConfigProperty;
import com.backend.domain.repository.PlatformConfigPropertyRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PlatformConfigPropertyRepositoryImpl implements PlatformConfigPropertyRepository {

    private final JpaPlatformConfigPropertyRepository jpaRepository;

    @Override
    public PlatformConfigProperty save(PlatformConfigProperty entity) {
        return toDomain(jpaRepository.save(toEntity(entity)));
    }

    @Override
    public Optional<PlatformConfigProperty> findByConfigKey(String configKey) {
        return jpaRepository.findByConfigKey(configKey).map(this::toDomain);
    }

    @Override
    public List<PlatformConfigProperty> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteByConfigKey(String configKey) {
        jpaRepository.deleteByConfigKey(configKey);
    }

    private PlatformConfigProperty toDomain(com.backend.infrastructure.persistence.platform.entity.PlatformConfigProperty source) {
        if (source == null) {
            return null;
        }
        PlatformConfigProperty target = new PlatformConfigProperty();
        target.setId(source.getId());
        target.setUuid(source.getUuid());
        target.setUid(source.getUid());
        target.setConfigKey(source.getConfigKey());
        target.setConfigValue(source.getConfigValue());
        target.setSecret(source.getSecret());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        target.setUpdatedBy(source.getUpdatedBy());
        return target;
    }

    private com.backend.infrastructure.persistence.platform.entity.PlatformConfigProperty toEntity(PlatformConfigProperty source) {
        if (source == null) {
            return null;
        }
        return com.backend.infrastructure.persistence.platform.entity.PlatformConfigProperty.builder()
                .id(source.getId())
                .uuid(source.getUuid())
                .uid(source.getUid())
                .configKey(source.getConfigKey())
                .configValue(source.getConfigValue())
                .secret(source.getSecret())
                .createdAt(source.getCreatedAt())
                .updatedAt(source.getUpdatedAt())
                .updatedBy(source.getUpdatedBy())
                .build();
    }
}
