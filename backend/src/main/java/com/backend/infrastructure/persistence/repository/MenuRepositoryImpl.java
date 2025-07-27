package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Menu;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepository {
    
    private final MenuJpaRepository menuJpaRepository;
    
    @Override
    public Menu save(Menu menu) {
        return menuJpaRepository.save(menu);
    }
    
    @Override
    public Optional<Menu> findById(Long id) {
        return menuJpaRepository.findById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return menuJpaRepository.existsById(id);
    }
    
    @Override
    public List<Menu> findBySiteId(Long siteId) {
        return menuJpaRepository.findBySiteId(siteId);
    }
    
    @Override
    public List<Menu> findBySiteIdOrderByLanguageAsc(Long siteId) {
        return menuJpaRepository.findBySiteIdOrderByLanguageAsc(siteId);
    }
    
    @Override
    public List<Menu> findBySiteIdAndLanguage(Long siteId, Language language) {
        return menuJpaRepository.findBySiteIdAndLanguage(siteId, language);
    }
    
    @Override
    public List<Menu> findByTenantId(Long tenantId) {
        return menuJpaRepository.findByTenantId(tenantId);
    }
    
    @Override
    public List<Menu> findByTenantIdAndLanguage(Long tenantId, Language language) {
        return menuJpaRepository.findByTenantIdAndLanguage(tenantId, language);
    }
    
    @Override
    public Optional<Menu> findByNameAndSiteIdAndLanguage(String name, Long siteId, Language language) {
        return menuJpaRepository.findByNameAndSiteIdAndLanguage(name, siteId, language);
    }
    
    @Override
    public List<Menu> findAll() {
        return menuJpaRepository.findAll();
    }
    
    @Override
    public void deleteById(Long id) {
        menuJpaRepository.deleteById(id);
    }
    
    @Override
    public void deleteBySiteId(Long siteId) {
        menuJpaRepository.deleteBySiteId(siteId);
    }
    
    @Override
    public boolean existsByNameAndSiteIdAndLanguage(String name, Long siteId, Language language) {
        return menuJpaRepository.existsByNameAndSiteIdAndLanguage(name, siteId, language);
    }
    
    @Override
    public boolean existsByNameAndSiteIdAndLanguageAndIdNot(String name, Long siteId, Language language, Long id) {
        return menuJpaRepository.existsByNameAndSiteIdAndLanguageAndIdNot(name, siteId, language, id);
    }
    
    @Override
    public long countBySiteId(Long siteId) {
        return menuJpaRepository.countBySiteId(siteId);
    }
    
    @Override
    public long countByTenantId(Long tenantId) {
        return menuJpaRepository.countByTenantId(tenantId);
    }
} 