package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Menu;
import com.backend.domain.enums.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuJpaRepository extends JpaRepository<Menu, Long> {
    
    // Site queries
    List<Menu> findBySiteId(Long siteId);
    List<Menu> findBySiteIdOrderByLanguageAsc(Long siteId);
    List<Menu> findBySiteIdAndLanguage(Long siteId, Language language);
    
    // Tenant queries
    List<Menu> findByTenantId(Long tenantId);
    List<Menu> findByTenantIdAndLanguage(Long tenantId, Language language);
    
    // Name and language queries
    Optional<Menu> findByNameAndSiteIdAndLanguage(String name, Long siteId, Language language);
    
    // Delete operations
    void deleteBySiteId(Long siteId);
    
    // Existence checks
    boolean existsByNameAndSiteIdAndLanguage(String name, Long siteId, Language language);
    boolean existsByNameAndSiteIdAndLanguageAndIdNot(String name, Long siteId, Language language, Long id);
    
    // Count queries
    long countBySiteId(Long siteId);
    long countByTenantId(Long tenantId);
} 