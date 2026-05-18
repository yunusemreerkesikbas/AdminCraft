package com.backend.infrastructure.persistence.platform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.infrastructure.persistence.platform.entity.PlatformOutreachTemplate;

@Repository
public interface PlatformOutreachTemplateRepository extends JpaRepository<PlatformOutreachTemplate, Long> {

    List<PlatformOutreachTemplate> findByIsActiveTrue();
}
