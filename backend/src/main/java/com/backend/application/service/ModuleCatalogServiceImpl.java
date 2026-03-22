package com.backend.application.service;

import com.backend.application.dto.provisioning.ModuleCatalogResponse;
import com.backend.domain.enums.ModuleCode;
import com.backend.infrastructure.persistence.platform.entity.ModuleCatalog;
import com.backend.infrastructure.persistence.platform.repository.ModuleCatalogRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ModuleCatalogServiceImpl implements ModuleCatalogService {

  private final ModuleCatalogRepository moduleCatalogRepository;
  private final ObjectMapper objectMapper;

  public ModuleCatalogServiceImpl(ModuleCatalogRepository moduleCatalogRepository,
          ObjectMapper objectMapper) {
    this.moduleCatalogRepository = moduleCatalogRepository;
    this.objectMapper = objectMapper;
  }

  @Override
  public List<ModuleCatalogResponse> getAllModules() {
    Map<String, ModuleCatalog> catalogByCode = moduleCatalogRepository.findAll().stream()
        .collect(Collectors.toMap(
            ModuleCatalog::getCode,
            Function.identity(),
            (existing, replacement) -> existing));
    return ModuleCode.provisioningSelectableCodes().stream()
        .map(catalogByCode::get)
        .filter(Objects::nonNull)
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  private ModuleCatalogResponse mapToResponse(ModuleCatalog module) {
    List<String> deps = Collections.emptyList();

    if (module.getDeps() != null && !module.getDeps().isBlank()) {
          try {
        deps = objectMapper.readValue(module.getDeps(), new TypeReference<List<String>>() {
        });
          } catch (Exception e) {
        String errorMsg = e.getMessage();
        log.warn("Failed to parse deps for module {}: {}", module.getCode(),
            errorMsg != null && errorMsg.length() > 500 ? errorMsg.substring(0, 500) : errorMsg);
          }
    }

    return ModuleCatalogResponse.builder()
        .code(module.getCode())
        .name(module.getName())
        .type(module.getType())
        .version(module.getVersion())
        .deps(deps)
        .enabledByDefault(module.getEnabledByDefault())
        .description(module.getDescription())
        .build();
  }
}
