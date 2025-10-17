package com.backend.application.service;

import com.backend.application.dto.provisioning.ModuleCatalogResponse;

import java.util.List;

public interface ModuleCatalogService {

  List<ModuleCatalogResponse> getAllModules();
}

