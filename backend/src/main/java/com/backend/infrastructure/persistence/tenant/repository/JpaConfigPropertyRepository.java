package com.backend.infrastructure.persistence.tenant.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.ConfigProperty;

public interface JpaConfigPropertyRepository extends JpaRepository<ConfigProperty, Long> {

    Optional<ConfigProperty> findByConfigKey(String configKey);

    @Transactional
    @Modifying
    @Query("DELETE FROM ConfigProperty c WHERE c.configKey = :configKey")
    void deleteByConfigKey(@Param("configKey") String configKey);
}

