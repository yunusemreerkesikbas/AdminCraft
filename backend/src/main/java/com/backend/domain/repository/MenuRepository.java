package com.backend.domain.repository;

import com.backend.domain.entity.Menu;
import com.backend.domain.enums.Language;

import java.util.List;
import java.util.Optional;

public interface MenuRepository {
    
    Menu save(Menu menu);
    
    Optional<Menu> findById(Long id);
    
    boolean existsById(Long id);
    
    List<Menu> findBySiteId(Long siteId);
    
    List<Menu> findBySiteIdOrderByLanguageAsc(Long siteId);
    
    List<Menu> findBySiteIdAndLanguage(Long siteId, Language language);
    
    List<Menu> findByTenantId(Long tenantId);
    
    List<Menu> findByTenantIdAndLanguage(Long tenantId, Language language);
    
    Optional<Menu> findByNameAndSiteIdAndLanguage(String name, Long siteId, Language language);
    
    List<Menu> findAll();
    
    void deleteById(Long id);
    
    void deleteBySiteId(Long siteId);
    
    boolean existsByNameAndSiteIdAndLanguage(String name, Long siteId, Language language);
    
    boolean existsByNameAndSiteIdAndLanguageAndIdNot(String name, Long siteId, Language language, Long id);
    
    long countBySiteId(Long siteId);
    
    long countByTenantId(Long tenantId);
}