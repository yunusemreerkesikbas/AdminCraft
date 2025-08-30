package com.backend.application.service;

import com.backend.domain.entity.PageCategory;
import com.backend.domain.enums.CategoryStatus;
import com.backend.domain.enums.Language;
import com.backend.domain.entity.Tenant;
import com.backend.domain.exception.CategoryNotFoundException;
import com.backend.domain.exception.TenantMismatchException;
import com.backend.domain.repository.PageCategoryRepository;
import com.backend.domain.repository.PageCategoryTranslationRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.presentation.dto.response.PageCategoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PageCategoryServiceImpl implements PageCategoryService {

  private final PageCategoryRepository categoryRepository;
  private final PageCategoryTranslationRepository translationRepository;
  private final TenantRepository tenantRepository;

  @Override
  public PageCategory create(PageCategory category) {
    if (categoryRepository.existsByTenantIdAndSlug(category.getTenantId(), category.getSlug())) {
      throw new IllegalArgumentException("Category slug already exists for tenant");
    }
    // Compute path and level
    String parentPath = null;
    Integer parentLevel = 0;
    if (category.getParentId() != null) {
      PageCategory parent = categoryRepository.findById(category.getParentId())
          .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));
      if (!Objects.equals(parent.getTenantId(), category.getTenantId())) {
        throw new IllegalArgumentException("Parent category tenant mismatch");
      }
      parentPath = parent.getPath();
      parentLevel = parent.getLevel() == null ? 0 : parent.getLevel();
    }
    String ownPath = (parentPath == null || parentPath.isBlank())
        ? "/" + category.getSlug()
        : parentPath + "/" + category.getSlug();
    category.setPath(ownPath);
    category.setLevel(parentLevel + 1);
    if (category.getStatus() == null) {
      category.setStatus(CategoryStatus.ACTIVE);
    }
    if (category.getSortOrder() == null) {
      category.setSortOrder(0);
    }
    return categoryRepository.save(category);
  }

  @Override
  public PageCategory update(PageCategory category) {
    PageCategory existing = categoryRepository.findById(category.getId())
        .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    if (!existing.getSlug().equals(category.getSlug()) &&
        categoryRepository.existsByTenantIdAndSlug(category.getTenantId(), category.getSlug())) {
      throw new IllegalArgumentException("Category slug already exists for tenant");
    }
    // Recompute path/level if slug or parent changed
    boolean parentChanged = !Objects.equals(existing.getParentId(), category.getParentId());
    boolean slugChanged = !Objects.equals(existing.getSlug(), category.getSlug());
    existing.setName(category.getName());
    existing.setSlug(category.getSlug());
    existing.setParentId(category.getParentId());
    existing.setSortOrder(category.getSortOrder());
    existing.setStatus(category.getStatus());

    if (parentChanged || slugChanged) {
      String parentPath = null;
      Integer parentLevel = 0;
      if (existing.getParentId() != null) {
        PageCategory parent = categoryRepository.findById(existing.getParentId())
            .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));
        parentPath = parent.getPath();
        parentLevel = parent.getLevel() == null ? 0 : parent.getLevel();
      }
      String newPath = (parentPath == null || parentPath.isBlank())
          ? "/" + existing.getSlug()
          : parentPath + "/" + existing.getSlug();
      String oldPath = existing.getPath();
      existing.setPath(newPath);
      existing.setLevel(parentLevel + 1);

      // Update descendants paths and levels
      List<PageCategory> descendants = categoryRepository.findByTenantIdAndPathStartingWith(
          existing.getTenantId(), oldPath + "/");
      for (PageCategory d : descendants) {
        String suffix = d.getPath().substring(oldPath.length());
        d.setPath(newPath + suffix);
        // recompute level based on number of '/' in path
        d.setLevel((int) newPath.chars().filter(ch -> ch == '/').count() +
            (int) suffix.chars().filter(ch -> ch == '/').count());
      }
      categoryRepository.saveAll(descendants);
    }
    return categoryRepository.save(existing);
  }

  @Override
  public void delete(Long id) {
    if (!categoryRepository.existsById(id)) {
      throw new IllegalArgumentException("Category not found");
    }
    categoryRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PageCategory> findById(Long id) {
    return categoryRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PageCategory> findByIdAndTenantId(Long id, Long tenantId) {
    return categoryRepository.findByIdAndTenantId(id, tenantId);
  }

  @Override
  @Transactional(readOnly = true)
  public void validateParentBelongsToTenant(Long parentId, Long tenantId) {
    if (parentId == null) {
      return; // Root category, no validation needed
    }
    
    Optional<PageCategory> parent = categoryRepository.findByIdAndTenantId(parentId, tenantId);
    if (parent.isEmpty()) {
      log.warn("SECURITY_ALERT: Attempt to use parent category {} from different tenant by tenant {}", 
               parentId, tenantId);
      throw new TenantMismatchException("Parent category does not belong to the specified tenant");
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<PageCategory> listByTenant(Long tenantId) {
    return categoryRepository.findByTenantIdAndParentIdIsNullOrderBySortOrderAsc(tenantId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PageCategory> listChildren(Long tenantId, Long parentId) {
    return categoryRepository.findByTenantIdAndParentIdOrderBySortOrderAsc(tenantId, parentId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PageCategoryDto> getTree(Long tenantId, String languageCode, Long rootId, Integer depth) {
    Language lang = resolveLanguage(tenantId, languageCode);
    List<PageCategory> cats;
    
    if (rootId == null) {
      cats = categoryRepository.findByTenantId(tenantId);
    } else {
      PageCategory root = categoryRepository.findByIdAndTenantId(rootId, tenantId)
          .orElseThrow(() -> new CategoryNotFoundException(rootId, tenantId));
      String prefix = root.getPath() + "/";
      cats = categoryRepository.findByTenantIdAndPathStartingWith(tenantId, prefix);
      cats.add(root);
    }
    
    // ÇÖZÜM: Batch loading ile N+1 query problemini çöz
    return toDtoLocalizedBatch(cats, lang, tenantId);
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE) // ÇÖZÜM: Race condition prevention
  public void move(Long tenantId, Long categoryId, Long newParentId) {
    log.info("SECURITY_AUDIT: Moving category {} to parent {} for tenant {}", categoryId, newParentId, tenantId);
    
    // GÜVENLIK: Tenant aware category lookup
    PageCategory node = categoryRepository.findByIdAndTenantId(categoryId, tenantId)
        .orElseThrow(() -> new CategoryNotFoundException(categoryId, tenantId));
    
    PageCategory newParent = null;
    if (newParentId != null) {
      // GÜVENLIK: Parent tenant validation
      newParent = categoryRepository.findByIdAndTenantId(newParentId, tenantId)
          .orElseThrow(() -> new CategoryNotFoundException(newParentId, tenantId));
      
      // ÇÖZÜM: Database-level cycle detection with locking
      validateNoCycleWithLocking(node, newParent);
    }

    String oldPath = node.getPath();
    
    // ÇÖZÜM: Path ve level calculation'ı Domain layer'a taşınmalı (TODO: Business logic refactor)
    String newPath = calculateNewPath(newParent, node.getSlug());
    int newLevel = calculateNewLevel(newParent);
    
    // Update node
    node.setParentId(newParentId);
    node.setPath(newPath);
    node.setLevel(newLevel);
    categoryRepository.save(node);

    // ÇÖZÜM: Bulk update descendants - daha performanslı
    updateDescendantPaths(tenantId, oldPath, newPath);
    
    log.info("SECURITY_AUDIT: Category move completed - categoryId={}, oldPath={}, newPath={}", 
             categoryId, oldPath, newPath);
  }
  
  /**
   * ÇÖZÜM: Thread-safe cycle detection with database locking.
   * SERIALIZABLE isolation level + path-based validation önler race condition'ları.
   */
  private void validateNoCycleWithLocking(PageCategory node, PageCategory newParent) {
    if (newParent == null) {
      return; // Root'a taşıma - cycle yok
    }
    
    // Path-based cycle detection - daha güvenilir
    if (node.getPath() != null && newParent.getPath() != null) {
      String newParentPathPrefix = newParent.getPath() + "/";
      if (newParent.getPath().startsWith(node.getPath() + "/") || 
          newParent.getPath().equals(node.getPath())) {
        throw new IllegalArgumentException("Cannot move category under its descendant - would create cycle");
      }
    }
  }
  
  /**
   * ÇÖZÜM: Business logic helper - Domain layer'a taşınmalı.
   */
  private String calculateNewPath(PageCategory parent, String slug) {
    if (parent == null || parent.getPath() == null || parent.getPath().isBlank()) {
      return "/" + slug;
    }
    return parent.getPath() + "/" + slug;
  }
  
  /**
   * ÇÖZÜM: Business logic helper - Domain layer'a taşınmalı.
   */
  private int calculateNewLevel(PageCategory parent) {
    return (parent == null || parent.getLevel() == null) ? 1 : parent.getLevel() + 1;
  }
  
  /**
   * ÇÖZÜM: Performans iyileştirmesi - bulk path update.
   */
  private void updateDescendantPaths(Long tenantId, String oldPath, String newPath) {
    List<PageCategory> descendants = categoryRepository.findByTenantIdAndPathStartingWith(
        tenantId, oldPath + "/");
    
    if (descendants.isEmpty()) {
      return;
    }
    
    // Batch path calculation
    for (PageCategory d : descendants) {
      String suffix = d.getPath().substring(oldPath.length());
      String newDescendantPath = newPath + suffix;
      d.setPath(newDescendantPath);
      // Level calculation optimization
      d.setLevel(Math.toIntExact(newDescendantPath.chars().filter(ch -> ch == '/').count()));
    }
    
    // Bulk save
    categoryRepository.saveAll(descendants);
    log.debug("Updated {} descendant paths for category move", descendants.size());
  }

  @Override
  public void reorder(Long tenantId, Long parentId, List<Long> orderedIds) {
    List<PageCategory> siblings = parentId == null
        ? categoryRepository.findByTenantIdAndParentIdIsNullOrderBySortOrderAsc(tenantId)
        : categoryRepository.findByTenantIdAndParentIdOrderBySortOrderAsc(tenantId, parentId);
    for (int i = 0; i < siblings.size(); i++) {
      PageCategory c = siblings.get(i);
      int newIndex = orderedIds.indexOf(c.getId());
      if (newIndex >= 0 && !Objects.equals(c.getSortOrder(), newIndex)) {
        c.setSortOrder(newIndex);
      }
    }
    categoryRepository.saveAll(siblings);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PageCategoryDto> listChildrenLocalized(Long tenantId, Long parentId, String languageCode) {
    Language lang = resolveLanguage(tenantId, languageCode);
    List<PageCategory> list = parentId == null
        ? categoryRepository.findByTenantIdAndParentIdIsNullOrderBySortOrderAsc(tenantId)
        : categoryRepository.findByTenantIdAndParentIdOrderBySortOrderAsc(tenantId, parentId);
    
    // ÇÖZÜM: Batch loading ile N+1 query problemini çöz
    return toDtoLocalizedBatch(list, lang, tenantId);
  }

  private Language resolveLanguage(Long tenantId, String code) {
    if (code != null) {
      try {
        return Language.valueOf(code.toUpperCase());
      } catch (Exception ignored) {
      }
    }
    return tenantRepository.findById(tenantId)
        .map(Tenant::getDefaultLanguage)
        .orElse(Language.TR);
  }

  /**
   * PERFORMANS İYİLEŞTİRMESİ: Batch loading ile N+1 query problemini çözer.
   * Tüm kategoriler için çevirileri tek sorguda yükler.
   */
  private List<PageCategoryDto> toDtoLocalizedBatch(List<PageCategory> categories, Language lang, Long tenantId) {
    if (categories.isEmpty()) {
      return List.of();
    }
    
    // Batch loading: Tüm kategori ID'lerini al
    List<Long> categoryIds = categories.stream()
        .map(PageCategory::getId)
        .toList();
    
    // Tek sorguda tüm çevirileri yükle
    List<com.backend.domain.entity.PageCategoryTranslation> translations = 
        translationRepository.findByTenantIdAndCategoryIdInAndLanguage(tenantId, categoryIds, lang);
    
    // Lookup map oluştur - O(1) erişim için
    Map<Long, com.backend.domain.entity.PageCategoryTranslation> translationMap = translations.stream()
        .collect(Collectors.toMap(
            com.backend.domain.entity.PageCategoryTranslation::getCategoryId, 
            Function.identity()));
    
    // DTO'ları oluştur - artık veritabanına additional query yok
    return categories.stream()
        .map(c -> toDtoLocalized(c, lang, translationMap))
        .toList();
  }

  /**
   * Single category DTO conversion with pre-loaded translations map.
   * Bu method artık N+1 query problemi yaratmaz.
   */
  private PageCategoryDto toDtoLocalized(PageCategory c, Language lang, 
                                       Map<Long, com.backend.domain.entity.PageCategoryTranslation> translationMap) {
    var tr = translationMap.get(c.getId());
    String name = tr != null ? tr.getName() : c.getName();
    String slug = tr != null ? tr.getSlug() : c.getSlug();
    return new PageCategoryDto(
        c.getId(), c.getTenantId(), c.getParentId(), name, slug,
        c.getPath(), c.getLevel(), c.getSortOrder(), c.getStatus());
  }
  
  /**
   * Fallback method for single category conversion - DEPRECATED.
   * Bu method N+1 query yaratır, sadece geriye dönük uyumluluk için.
   */
  @Deprecated
  private PageCategoryDto toDtoLocalized(PageCategory c, Language lang) {
    log.warn("PERFORMANCE_WARNING: Using deprecated single translation lookup - potential N+1 query for category {}", c.getId());
    var tr = translationRepository.findByTenantIdAndCategoryIdAndLanguage(
        c.getTenantId(), c.getId(), lang).orElse(null);
    String name = tr != null ? tr.getName() : c.getName();
    String slug = tr != null ? tr.getSlug() : c.getSlug();
    return new PageCategoryDto(
        c.getId(), c.getTenantId(), c.getParentId(), name, slug,
        c.getPath(), c.getLevel(), c.getSortOrder(), c.getStatus());
  }
}
