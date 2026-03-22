package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Component;

import com.backend.domain.entity.TenantModule;
import com.backend.domain.repository.TenantModuleRepository;

import lombok.RequiredArgsConstructor;

@Component("tenantModuleDomainRepository")
@RequiredArgsConstructor
public class TenantModuleRepositoryImpl implements TenantModuleRepository {

    private final com.backend.infrastructure.persistence.platform.repository.TenantModuleRepository jpaRepository;

    @Override
    public List<TenantModule> findByTenantId(Long tenantId) {
        return jpaRepository.findByTenantId(tenantId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByTenantIdAndModuleCodeAndStatus(Long tenantId, String moduleCode, String status) {
		return jpaRepository.existsByTenantIdAndModuleCodeAndStatus(tenantId, moduleCode, status);
    }

    @Override
    public List<TenantModule> saveAll(Iterable<TenantModule> modules) {
        List<com.backend.infrastructure.persistence.platform.entity.TenantModule> platformModules =
                StreamSupport.stream(modules.spliterator(), false)
                        .map(this::toPlatform)
                        .collect(Collectors.toList());
        return jpaRepository.saveAll(platformModules).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private TenantModule toDomain(com.backend.infrastructure.persistence.platform.entity.TenantModule source) {
        if (source == null) return null;
        return TenantModule.builder()
                .id(source.getId())
                .tenantId(source.getTenantId())
                .moduleCode(source.getModuleCode())
                .status(source.getStatus())
                .targetVersion(source.getTargetVersion())
                .installedAt(source.getInstalledAt())
                .updatedAt(source.getUpdatedAt())
                .build();
    }

    private com.backend.infrastructure.persistence.platform.entity.TenantModule toPlatform(TenantModule source) {
        if (source == null) return null;
        return com.backend.infrastructure.persistence.platform.entity.TenantModule.builder()
                .id(source.getId())
                .tenantId(source.getTenantId())
                .moduleCode(source.getModuleCode())
                .status(source.getStatus())
                .targetVersion(source.getTargetVersion())
                .installedAt(source.getInstalledAt())
                .updatedAt(source.getUpdatedAt())
                .build();
    }
}
