package com.backend.application.service;

import com.backend.domain.entity.PageCategory;
import com.backend.domain.enums.CategoryStatus;
import com.backend.domain.enums.Language;
import com.backend.domain.entity.Tenant;
import com.backend.domain.repository.PageCategoryRepository;
import com.backend.domain.repository.PageCategoryTranslationRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.presentation.dto.response.PageCategoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
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
      PageCategory root = categoryRepository.findById(rootId)
          .orElseThrow(() -> new IllegalArgumentException("Root category not found"));
      String prefix = root.getPath() + "/";
      cats = categoryRepository.findByTenantIdAndPathStartingWith(tenantId, prefix);
      cats.add(root);
    }
    return cats.stream().map(c -> toDtoLocalized(c, lang)).toList();
  }

  @Override
  public void move(Long tenantId, Long categoryId, Long newParentId) {
    PageCategory node = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    if (!Objects.equals(node.getTenantId(), tenantId)) {
      throw new IllegalArgumentException("Tenant mismatch");
    }
    PageCategory newParent = null;
    if (newParentId != null) {
      newParent = categoryRepository.findById(newParentId)
          .orElseThrow(() -> new IllegalArgumentException("New parent not found"));
      if (!Objects.equals(newParent.getTenantId(), tenantId)) {
        throw new IllegalArgumentException("Parent tenant mismatch");
      }
      // Prevent cycles
      String np = newParent.getPath() + "/";
      if (node.getPath() != null && node.getPath().startsWith(np)) {
        throw new IllegalArgumentException("Cannot move a node under its descendant");
      }
    }

    Long oldParentId = node.getParentId();
    String oldPath = node.getPath();

    node.setParentId(newParentId);
    String newPath = (newParent == null || newParent.getPath() == null || newParent.getPath().isBlank())
        ? "/" + node.getSlug()
        : newParent.getPath() + "/" + node.getSlug();
    int newLevel = (newParent == null || newParent.getLevel() == null) ? 1 : newParent.getLevel() + 1;
    node.setPath(newPath);
    node.setLevel(newLevel);
    categoryRepository.save(node);

    // Update descendants
    List<PageCategory> descendants = categoryRepository.findByTenantIdAndPathStartingWith(
        tenantId, oldPath + "/");
    for (PageCategory d : descendants) {
      String suffix = d.getPath().substring(oldPath.length());
      d.setPath(newPath + suffix);
      d.setLevel((int) (newPath.chars().filter(ch -> ch == '/').count()) +
          (int) (suffix.chars().filter(ch -> ch == '/').count()));
    }
    categoryRepository.saveAll(descendants);
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
    return list.stream().map(c -> toDtoLocalized(c, lang)).toList();
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

  private PageCategoryDto toDtoLocalized(PageCategory c, Language lang) {
    var tr = translationRepository.findByTenantIdAndCategoryIdAndLanguage(
        c.getTenantId(), c.getId(), lang).orElse(null);
    String name = tr != null ? tr.getName() : c.getName();
    String slug = tr != null ? tr.getSlug() : c.getSlug();
    return new PageCategoryDto(
        c.getId(), c.getTenantId(), c.getParentId(), name, slug,
        c.getPath(), c.getLevel(), c.getSortOrder(), c.getStatus());
  }
}
