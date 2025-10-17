package com.backend.infrastructure.persistence.platform.repository;

import com.backend.infrastructure.persistence.platform.entity.ModuleCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleCatalogRepository extends JpaRepository<ModuleCatalog, Long> {

  Optional<ModuleCatalog> findByCode(String code);

  List<ModuleCatalog> findByType(String type);

  List<ModuleCatalog> findByEnabledByDefaultTrue();
}

