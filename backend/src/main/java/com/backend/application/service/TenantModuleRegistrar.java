package com.backend.application.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.TenantModule;
import com.backend.domain.enums.ModuleCode;
import com.backend.domain.repository.TenantModuleRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TenantModuleRegistrar {

    private static final String MODULE_STATUS_ENABLED = "enabled";

    private final TenantModuleRepository tenantModuleRepository;

    public TenantModuleRegistrar(
            @Qualifier("tenantModuleDomainRepository") TenantModuleRepository tenantModuleRepository) {
        this.tenantModuleRepository = tenantModuleRepository;
    }

    @Transactional("platformTransactionManager")
    public void registerModules(Long tenantId, List<String> modules) {
        log.info("[PROVISION] registerModules called — tenantId={} modules={}", tenantId, modules);

        List<String> existingCodes = tenantModuleRepository.findByTenantId(tenantId)
                .stream().map(TenantModule::getModuleCode).toList();
        log.debug("[PROVISION] existingCodes={}", existingCodes);

        LocalDateTime now = LocalDateTime.now();
        List<TenantModule> newModules = modules.stream()
                .filter(code -> !existingCodes.contains(code))
                .filter(code -> {
                    if (!ModuleCode.isValidCode(code)) {
                        log.warn("[PROVISION] Skipping unknown module code: {}", code);
                        return false;
                    }
                    return true;
                })
                .map(code -> TenantModule.builder()
                        .tenantId(tenantId)
                        .moduleCode(code)
                        .status(MODULE_STATUS_ENABLED)
                        .installedAt(now)
                        .build())
                .toList();

        if (!newModules.isEmpty()) {
            tenantModuleRepository.saveAll(newModules);
            log.info("[PROVISION] Registered {} modules for tenant {}: {}",
                    newModules.size(), tenantId,
                    newModules.stream().map(TenantModule::getModuleCode).toList());
        } else {
            log.info("[PROVISION] No new modules to register for tenant {}", tenantId);
        }
    }
}
