package com.backend.application.service;

import com.backend.domain.entity.PageCategory;
import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.CategoryStatus;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.PageCategoryRepository;
import com.backend.domain.repository.PageCategoryTranslationRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.presentation.dto.response.PageCategoryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PageCategoryServiceImplTest {

  @Mock
  private PageCategoryRepository categoryRepo;
  @Mock
  private PageCategoryTranslationRepository trRepo;
  @Mock
  private TenantRepository tenantRepo;

  private PageCategoryService service;

  @BeforeEach
  void setUp() {
    service = new PageCategoryServiceImpl(categoryRepo, trRepo, tenantRepo);
  }

  @Test
  void create_root_setsPathLevelAndDefaults() {
    when(categoryRepo.existsByTenantIdAndSlug(1L, "root")).thenReturn(false);
    when(categoryRepo.save(any())).thenAnswer(i -> i.getArgument(0));

    PageCategory c = new PageCategory();
    c.setTenantId(1L);
    c.setSlug("root");
    c.setName("Root");

    PageCategory saved = service.create(c);

    assertThat(saved.getPath()).isEqualTo("/root");
    assertThat(saved.getLevel()).isEqualTo(1);
    assertThat(saved.getSortOrder()).isZero();
    assertThat(saved.getStatus()).isEqualTo(CategoryStatus.ACTIVE);
  }

  @Test
  void create_child_setsPathAndLevel_fromParent() {
    PageCategory parent = new PageCategory();
    parent.setId(10L);
    parent.setTenantId(1L);
    parent.setSlug("root");
    parent.setPath("/root");
    parent.setLevel(1);
    when(categoryRepo.findById(10L)).thenReturn(Optional.of(parent));
    when(categoryRepo.existsByTenantIdAndSlug(1L, "child")).thenReturn(false);
    when(categoryRepo.save(any())).thenAnswer(i -> i.getArgument(0));

    PageCategory c = new PageCategory();
    c.setTenantId(1L);
    c.setSlug("child");
    c.setName("Child");
    c.setParentId(10L);

    PageCategory saved = service.create(c);
    assertThat(saved.getPath()).isEqualTo("/root/child");
    assertThat(saved.getLevel()).isEqualTo(2);
  }

  @Test
  void update_slug_propagates_to_descendants() {
    PageCategory existing = new PageCategory();
    existing.setId(11L);
    existing.setTenantId(1L);
    existing.setSlug("old");
    existing.setPath("/root/old");
    existing.setLevel(2);
    when(categoryRepo.findById(11L)).thenReturn(Optional.of(existing));
    when(categoryRepo.existsByTenantIdAndSlug(1L, "new")).thenReturn(false);

    PageCategory grand = new PageCategory();
    grand.setId(12L);
    grand.setTenantId(1L);
    grand.setSlug("grand");
    grand.setPath("/root/old/grand");
    grand.setLevel(3);
    when(categoryRepo.findByTenantIdAndPathStartingWith(1L, "/root/old/"))
        .thenReturn(List.of(grand));

    when(categoryRepo.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
    when(categoryRepo.save(any())).thenAnswer(i -> i.getArgument(0));

    PageCategory update = new PageCategory();
    update.setId(11L);
    update.setTenantId(1L);
    update.setSlug("new");
    update.setName("New");

    PageCategory updated = service.update(update);
    assertThat(updated.getPath()).isEqualTo("/root/new");

    ArgumentCaptor<List<PageCategory>> cap = ArgumentCaptor.forClass(List.class);
    verify(categoryRepo).saveAll(cap.capture());
    PageCategory changed = cap.getValue().get(0);
    assertThat(changed.getPath()).isEqualTo("/root/new/grand");
    assertThat(changed.getLevel()).isGreaterThanOrEqualTo(3);
  }

  @Test
  void move_prevents_cycle() {
    PageCategory node = new PageCategory();
    node.setId(20L);
    node.setTenantId(1L);
    node.setSlug("a");
    node.setPath("/root/a");

    PageCategory desc = new PageCategory();
    desc.setId(21L);
    desc.setTenantId(1L);
    desc.setSlug("x");
    desc.setPath("/root/a/x");

    when(categoryRepo.findById(20L)).thenReturn(Optional.of(node));
    when(categoryRepo.findById(21L)).thenReturn(Optional.of(desc));

    assertThatThrownBy(() -> service.move(1L, 20L, 21L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void reorder_updates_sortOrder_from_orderedIds() {
    PageCategory c1 = new PageCategory();
    c1.setId(1L);
    c1.setTenantId(1L);
    c1.setSortOrder(0);
    PageCategory c2 = new PageCategory();
    c2.setId(2L);
    c2.setTenantId(1L);
    c2.setSortOrder(1);

    when(categoryRepo.findByTenantIdAndParentIdIsNullOrderBySortOrderAsc(1L))
        .thenReturn(List.of(c1, c2));

    when(categoryRepo.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

    service.reorder(1L, null, List.of(2L, 1L));

    assertThat(c1.getSortOrder()).isEqualTo(1);
    assertThat(c2.getSortOrder()).isEqualTo(0);
  }

  @Test
  void listChildrenLocalized_uses_fallback_default_language() {
    Tenant t = new Tenant();
    t.setId(1L);
    t.setDefaultLanguage(Language.TR);
    when(tenantRepo.findById(1L)).thenReturn(Optional.of(t));

    PageCategory c = new PageCategory();
    c.setId(100L);
    c.setTenantId(1L);
    c.setName("Kategori");
    c.setSlug("kategori");
    when(categoryRepo.findByTenantIdAndParentIdIsNullOrderBySortOrderAsc(1L))
        .thenReturn(List.of(c));

    // No translation found, should use entity fields
    when(trRepo.findByTenantIdAndCategoryIdAndLanguage(1L, 100L, Language.TR))
        .thenReturn(Optional.empty());

    List<PageCategoryDto> res = service.listChildrenLocalized(1L, null, "xx");
    assertThat(res).hasSize(1);
    assertThat(res.get(0).name()).isEqualTo("Kategori");
    assertThat(res.get(0).slug()).isEqualTo("kategori");
  }
}
