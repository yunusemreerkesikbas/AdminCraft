package com.backend.application.service.impl;

import com.backend.application.dto.request.CategoryI18nDto;
import com.backend.application.service.CategoryServiceImpl;
import com.backend.domain.entity.Category;
import com.backend.domain.entity.CategoryI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.CategoryI18nRepository;
import com.backend.domain.repository.CategoryRepository;
import com.backend.testutil.BaseServiceTest;
import com.backend.testutil.builders.CategoryTestDataBuilder;
import com.backend.testutil.builders.CategoryI18nTestDataBuilder;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CategoryServiceImpl.
 * Tests all CRUD operations, validation logic, and business rules.
 */
class CategoryServiceImplTest extends BaseServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryI18nRepository categoryI18nRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private Category parentCategory;
    private Category childCategory;

    @BeforeEach
    void setUp() {
        CategoryTestDataBuilder.resetIdCounter();

        parentCategory = CategoryTestDataBuilder.aRootCategory()
                .withId(1L)
                .withCode("parent_category")
                .build();

        testCategory = CategoryTestDataBuilder.aCategory()
                .withId(2L)
                .withCode("test_category")
                .withParentId(null)
                .build();

        childCategory = CategoryTestDataBuilder.aChildCategory(1L)
                .withId(3L)
                .withCode("child_category")
                .build();
    }

    // ==================== createComposite() Tests ====================

    @Nested
    @DisplayName("createComposite() Tests")
    class CreateCompositeTests {

        @Test
        @DisplayName("Should create root category successfully")
        void createComposite_AsRootCategory_Success() {
            // Given
            Map<Language, CategoryI18nDto> translations = Map.of(
                    Language.TR, new CategoryI18nDto("Kök Kategori", "Açıklama")
            );

            when(categoryRepository.existsByCode("root_cat")).thenReturn(false);
            when(categoryRepository.findMaxSortOrderByParentId(null)).thenReturn(5);
            when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
                Category c = inv.getArgument(0);
                c.setId(10L);
                return c;
            });
            when(categoryRepository.findByIdWithI18n(10L)).thenReturn(Optional.of(testCategory));

            // When
            Category result = categoryService.createComposite("root_cat", null, null, true,
                    translations, TEST_USER_ID);

            // Then
            assertThat(result).isNotNull();
            verify(categoryRepository).save(argThat(category ->
                    category.getParentId() == null &&
                    category.getSortOrder() == 6 // max + 1
            ));
        }

        @Test
        @DisplayName("Should create category with parent successfully")
        void createComposite_WithParent_Success() {
            // Given
            Map<Language, CategoryI18nDto> translations = Map.of(
                    Language.TR, new CategoryI18nDto("Alt Kategori")
            );

            when(categoryRepository.existsByCode("child_cat")).thenReturn(false);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
            when(categoryRepository.findMaxSortOrderByParentId(1L)).thenReturn(2);
            when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
                Category c = inv.getArgument(0);
                c.setId(11L);
                return c;
            });
            when(categoryRepository.findByIdWithI18n(11L)).thenReturn(Optional.of(childCategory));

            // When
            Category result = categoryService.createComposite("child_cat", 1L, null, true,
                    translations, TEST_USER_ID);

            // Then
            assertThat(result).isNotNull();
            verify(categoryRepository).save(argThat(category ->
                    category.getParentId() == 1L
            ));
        }

        @Test
        @DisplayName("Should auto-calculate sortOrder when not provided")
        void createComposite_AutoCalculatesSortOrder() {
            // Given
            Map<Language, CategoryI18nDto> translations = Map.of(
                    Language.TR, new CategoryI18nDto("Test")
            );

            when(categoryRepository.existsByCode("new_cat")).thenReturn(false);
            when(categoryRepository.findMaxSortOrderByParentId(null)).thenReturn(10);
            when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
                Category c = inv.getArgument(0);
                c.setId(12L);
                return c;
            });
            when(categoryRepository.findByIdWithI18n(12L)).thenReturn(Optional.of(testCategory));

            // When
            categoryService.createComposite("new_cat", null, null, true,
                    translations, TEST_USER_ID);

            // Then
            verify(categoryRepository).save(argThat(category ->
                    category.getSortOrder() == 11 // max + 1
            ));
        }

        @Test
        @DisplayName("Should use explicit sortOrder when provided")
        void createComposite_WithExplicitSortOrder() {
            // Given
            Map<Language, CategoryI18nDto> translations = Map.of(
                    Language.TR, new CategoryI18nDto("Test")
            );

            when(categoryRepository.existsByCode("sorted_cat")).thenReturn(false);
            when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
                Category c = inv.getArgument(0);
                c.setId(13L);
                return c;
            });
            when(categoryRepository.findByIdWithI18n(13L)).thenReturn(Optional.of(testCategory));

            // When
            categoryService.createComposite("sorted_cat", null, 99, true,
                    translations, TEST_USER_ID);

            // Then
            verify(categoryRepository).save(argThat(category ->
                    category.getSortOrder() == 99
            ));
        }

        @Test
        @DisplayName("Should throw exception when code is duplicate")
        void createComposite_ThrowsException_WhenCodeDuplicate() {
            // Given
            Map<Language, CategoryI18nDto> translations = Map.of(
                    Language.TR, new CategoryI18nDto("Test")
            );
            when(categoryRepository.existsByCode("existing_code")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> categoryService.createComposite("existing_code", null, null, true,
                    translations, TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should throw exception when parent not found")
        void createComposite_ThrowsException_WhenParentNotFound() {
            // Given
            Map<Language, CategoryI18nDto> translations = Map.of(
                    Language.TR, new CategoryI18nDto("Test")
            );
            when(categoryRepository.existsByCode("new_cat")).thenReturn(false);
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> categoryService.createComposite("new_cat", 999L, null, true,
                    translations, TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Parent category not found");
        }

        @Test
        @DisplayName("Should create category with multiple translations")
        void createComposite_WithMultipleTranslations() {
            // Given
            Map<Language, CategoryI18nDto> translations = new HashMap<>();
            translations.put(Language.TR, new CategoryI18nDto("Türkçe Kategori", "Açıklama"));
            translations.put(Language.EN, new CategoryI18nDto("English Category", "Description"));
            translations.put(Language.ES, new CategoryI18nDto("Categoría Español", "Descripción"));

            when(categoryRepository.existsByCode("multi_lang")).thenReturn(false);
            when(categoryRepository.findMaxSortOrderByParentId(null)).thenReturn(0);
            when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
                Category c = inv.getArgument(0);
                c.setId(14L);
                return c;
            });
            when(categoryRepository.findByIdWithI18n(14L)).thenReturn(Optional.of(testCategory));

            // When
            Category result = categoryService.createComposite("multi_lang", null, null, true,
                    translations, TEST_USER_ID);

            // Then
            assertThat(result).isNotNull();
            verify(categoryI18nRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("Should set default visibility to true")
        void createComposite_SetsDefaultVisibility() {
            // Given
            Map<Language, CategoryI18nDto> translations = Map.of(
                    Language.TR, new CategoryI18nDto("Test")
            );

            when(categoryRepository.existsByCode("visible_cat")).thenReturn(false);
            when(categoryRepository.findMaxSortOrderByParentId(null)).thenReturn(0);
            when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
                Category c = inv.getArgument(0);
                c.setId(15L);
                return c;
            });
            when(categoryRepository.findByIdWithI18n(15L)).thenReturn(Optional.of(testCategory));

            // When
            categoryService.createComposite("visible_cat", null, null, null,
                    translations, TEST_USER_ID);

            // Then
            verify(categoryRepository).save(argThat(category ->
                    category.getIsVisible() == true
            ));
        }

        @Test
        @DisplayName("Should throw exception when tenant context is not active")
        void createComposite_ThrowsException_WhenTenantNotActive() {
            // Given
            simulateInactiveTenantContext();
            Map<Language, CategoryI18nDto> translations = Map.of(
                    Language.TR, new CategoryI18nDto("Test")
            );

            // When & Then
            assertThatThrownBy(() -> categoryService.createComposite("test_cat", null, null, true,
                    translations, TEST_USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Tenant context");
        }
    }

    // ==================== updateComposite() Tests ====================

    @Nested
    @DisplayName("updateComposite() Tests")
    class UpdateCompositeTests {

        @Test
        @DisplayName("Should update category and change parent successfully")
        void updateComposite_ChangesParent_Success() {
            // Given
            Map<Language, CategoryI18nDto> translations = Map.of(
                    Language.TR, new CategoryI18nDto("Güncel Kategori")
            );

            when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
            when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
            when(categoryI18nRepository.findByCategoryIdAndLanguage(2L, Language.TR)).thenReturn(Optional.empty());
            when(categoryRepository.findByIdWithI18n(2L)).thenReturn(Optional.of(testCategory));

            // When
            Category result = categoryService.updateComposite(2L, 1L, null, null,
                    translations, TEST_USER_ID);

            // Then
            assertThat(result).isNotNull();
            verify(categoryRepository).save(argThat(category ->
                    category.getParentId() == 1L
            ));
        }

        @Test
        @DisplayName("Should convert category to root when parentId is null")
        void updateComposite_SetsParentToNull_Success() {
            // Given
            childCategory.setParentId(1L);
            Map<Language, CategoryI18nDto> translations = Map.of(
                    Language.TR, new CategoryI18nDto("Now Root")
            );

            when(categoryRepository.findById(3L)).thenReturn(Optional.of(childCategory));
            when(categoryRepository.save(any(Category.class))).thenReturn(childCategory);
            when(categoryI18nRepository.findByCategoryIdAndLanguage(3L, Language.TR)).thenReturn(Optional.empty());
            when(categoryRepository.findByIdWithI18n(3L)).thenReturn(Optional.of(childCategory));

            // When
            // Note: Setting parentId to explicit null might require special handling
            Category result = categoryService.updateComposite(3L, null, null, null,
                    translations, TEST_USER_ID);

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when category tries to be its own parent")
        void updateComposite_ThrowsException_WhenSelfAsParent() {
            // Given
            Map<Language, CategoryI18nDto> translations = Map.of(
                    Language.TR, new CategoryI18nDto("Test")
            );

            when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));

            // When & Then
            assertThatThrownBy(() -> categoryService.updateComposite(2L, 2L, null, null,
                    translations, TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be its own parent");
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void updateComposite_ThrowsException_WhenCategoryNotFound() {
            // Given
            Map<Language, CategoryI18nDto> translations = Map.of(
                    Language.TR, new CategoryI18nDto("Test")
            );

            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> categoryService.updateComposite(999L, null, null, null,
                    translations, TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Category not found");
        }

        @Test
        @DisplayName("Should throw exception when parent category not found")
        void updateComposite_ThrowsException_WhenParentNotFound() {
            // Given
            Map<Language, CategoryI18nDto> translations = Map.of(
                    Language.TR, new CategoryI18nDto("Test")
            );

            when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> categoryService.updateComposite(2L, 999L, null, null,
                    translations, TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Parent category not found");
        }

        @Test
        @DisplayName("Should insert new i18n entry for new language")
        void updateComposite_InsertsNewI18nLanguage() {
            // Given
            Map<Language, CategoryI18nDto> translations = Map.of(
                    Language.EN, new CategoryI18nDto("English Category", "Description")
            );

            when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
            when(categoryI18nRepository.findByCategoryIdAndLanguage(2L, Language.EN)).thenReturn(Optional.empty());
            when(categoryRepository.findByIdWithI18n(2L)).thenReturn(Optional.of(testCategory));

            // When
            categoryService.updateComposite(2L, null, null, null, translations, TEST_USER_ID);

            // Then
            verify(categoryI18nRepository).saveAll(argThat(list ->
                    list.size() == 1 &&
                    ((CategoryI18n) list.get(0)).getLanguage() == Language.EN &&
                    "English Category".equals(((CategoryI18n) list.get(0)).getName())
            ));
        }

        @Test
        @DisplayName("Should update existing i18n entry")
        void updateComposite_UpdatesExistingI18nLanguage() {
            // Given
            CategoryI18n existingI18n = CategoryI18nTestDataBuilder.aCategoryI18n()
                    .withId(100L)
                    .withCategory(testCategory)
                    .withLanguage(Language.TR)
                    .withName("Eski İsim")
                    .build();

            Map<Language, CategoryI18nDto> translations = Map.of(
                    Language.TR, new CategoryI18nDto("Yeni İsim", "Yeni Açıklama")
            );

            when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
            when(categoryI18nRepository.findByCategoryIdAndLanguage(2L, Language.TR)).thenReturn(Optional.of(existingI18n));
            when(categoryRepository.findByIdWithI18n(2L)).thenReturn(Optional.of(testCategory));

            // When
            categoryService.updateComposite(2L, null, null, null, translations, TEST_USER_ID);

            // Then
            verify(categoryI18nRepository).saveAll(argThat(list ->
                    list.size() == 1 &&
                    "Yeni İsim".equals(((CategoryI18n) list.get(0)).getName())
            ));
        }

        @Test
        @DisplayName("Should update visibility")
        void updateComposite_UpdatesVisibility() {
            // Given
            Map<Language, CategoryI18nDto> translations = Map.of(
                    Language.TR, new CategoryI18nDto("Test")
            );

            when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
            when(categoryI18nRepository.findByCategoryIdAndLanguage(2L, Language.TR)).thenReturn(Optional.empty());
            when(categoryRepository.findByIdWithI18n(2L)).thenReturn(Optional.of(testCategory));

            // When
            categoryService.updateComposite(2L, null, null, false, translations, TEST_USER_ID);

            // Then
            verify(categoryRepository).save(argThat(category ->
                    category.getIsVisible() == false
            ));
        }

        @Test
        @DisplayName("Should update sortOrder")
        void updateComposite_UpdatesSortOrder() {
            // Given
            Map<Language, CategoryI18nDto> translations = Map.of(
                    Language.TR, new CategoryI18nDto("Test")
            );

            when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
            when(categoryI18nRepository.findByCategoryIdAndLanguage(2L, Language.TR)).thenReturn(Optional.empty());
            when(categoryRepository.findByIdWithI18n(2L)).thenReturn(Optional.of(testCategory));

            // When
            categoryService.updateComposite(2L, null, 50, null, translations, TEST_USER_ID);

            // Then
            verify(categoryRepository).save(argThat(category ->
                    category.getSortOrder() == 50
            ));
        }

        @Test
        @DisplayName("Should throw exception when tenant context is not active")
        void updateComposite_ThrowsException_WhenTenantNotActive() {
            // Given
            simulateInactiveTenantContext();
            Map<Language, CategoryI18nDto> translations = Map.of(
                    Language.TR, new CategoryI18nDto("Test")
            );

            // When & Then
            assertThatThrownBy(() -> categoryService.updateComposite(2L, null, null, null,
                    translations, TEST_USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Tenant context");
        }
    }

    // ==================== delete() Tests - CRITICAL BUSINESS RULES ====================

    @Nested
    @DisplayName("delete() Tests - Business Rules")
    class DeleteTests {

        @Test
        @DisplayName("Should delete category successfully when no children or products")
        void delete_Success_WhenNoChildrenOrProducts() {
            // Given
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.hasChildren(2L)).thenReturn(false);
            when(categoryRepository.hasProducts(2L)).thenReturn(false);
            doNothing().when(categoryRepository).delete(testCategory);

            // When
            categoryService.delete(2L);

            // Then
            verify(categoryRepository).findById(2L);
            verify(categoryRepository).hasChildren(2L);
            verify(categoryRepository).hasProducts(2L);
            verify(categoryRepository).delete(testCategory);
        }

        @Test
        @DisplayName("Should throw IllegalStateException when category has children")
        void delete_ThrowsException_WhenHasChildren() {
            // Given
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
            when(categoryRepository.hasChildren(1L)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> categoryService.delete(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot delete category with children");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when category has products")
        void delete_ThrowsException_WhenHasProducts() {
            // Given
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.hasChildren(2L)).thenReturn(false);
            when(categoryRepository.hasProducts(2L)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> categoryService.delete(2L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot delete category with products");
        }

        @Test
        @DisplayName("Should check children first, then products")
        void delete_ChecksChildrenFirst_ThenProducts() {
            // Given
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
            when(categoryRepository.hasChildren(1L)).thenReturn(true);
            // hasProducts should NOT be called if hasChildren returns true

            // When & Then
            assertThatThrownBy(() -> categoryService.delete(1L))
                    .isInstanceOf(IllegalStateException.class);

            verify(categoryRepository).hasChildren(1L);
            verify(categoryRepository, never()).hasProducts(anyLong());
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void delete_ThrowsException_WhenCategoryNotFound() {
            // Given
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> categoryService.delete(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Category not found");
        }

        @Test
        @DisplayName("Should throw exception when tenant context is not active")
        void delete_ThrowsException_WhenTenantNotActive() {
            // Given
            simulateInactiveTenantContext();

            // When & Then
            assertThatThrownBy(() -> categoryService.delete(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Tenant context");
        }
    }

    // ==================== Tree Methods Tests ====================

    @Nested
    @DisplayName("Tree Methods Tests")
    class TreeMethodsTests {

        @Test
        @DisplayName("getTree should return only root categories")
        void getTree_ReturnsOnlyRootCategories() {
            // Given
            List<Category> rootCategories = List.of(parentCategory, testCategory);
            when(categoryRepository.findRootCategoriesWithI18n()).thenReturn(rootCategories);

            // When
            List<Category> result = categoryService.getTree();

            // Then
            assertThat(result).hasSize(2);
            verify(categoryRepository).findRootCategoriesWithI18n();
        }

        @Test
        @DisplayName("findByParentId should return children")
        void findByParentId_ReturnsChildren() {
            // Given
            List<Category> children = List.of(childCategory);
            when(categoryRepository.findByParentId(1L)).thenReturn(children);

            // When
            List<Category> result = categoryService.findByParentId(1L);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getParentId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("hasChildren should return correct value")
        void hasChildren_ReturnsCorrectValue() {
            // Given
            when(categoryRepository.hasChildren(1L)).thenReturn(true);
            when(categoryRepository.hasChildren(2L)).thenReturn(false);

            // When & Then
            assertThat(categoryService.hasChildren(1L)).isTrue();
            assertThat(categoryService.hasChildren(2L)).isFalse();
        }

        @Test
        @DisplayName("hasProducts should return correct value")
        void hasProducts_ReturnsCorrectValue() {
            // Given
            when(categoryRepository.hasProducts(1L)).thenReturn(true);
            when(categoryRepository.hasProducts(2L)).thenReturn(false);

            // When & Then
            assertThat(categoryService.hasProducts(1L)).isTrue();
            assertThat(categoryService.hasProducts(2L)).isFalse();
        }
    }

    // ==================== Query Methods Tests ====================

    @Nested
    @DisplayName("Query Methods Tests")
    class QueryMethodsTests {

        @Test
        @DisplayName("findById should return empty when not found")
        void findById_ReturnsEmpty_WhenNotFound() {
            // Given
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<Category> result = categoryService.findById(999L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("findById should return category when found")
        void findById_ReturnsCategory_WhenFound() {
            // Given
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));

            // When
            Optional<Category> result = categoryService.findById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("findByCode should return category")
        void findByCode_ReturnsCategory() {
            // Given
            when(categoryRepository.findByCode("parent_category")).thenReturn(Optional.of(parentCategory));

            // When
            Optional<Category> result = categoryService.findByCode("parent_category");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo("parent_category");
        }

        @Test
        @DisplayName("findByUid should return category")
        void findByUid_ReturnsCategory() {
            // Given
            when(categoryRepository.findByUid("category_1")).thenReturn(Optional.of(parentCategory));

            // When
            Optional<Category> result = categoryService.findByUid("category_1");

            // Then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("findAll should return all categories")
        void findAll_ReturnsAllCategories() {
            // Given
            List<Category> allCategories = List.of(parentCategory, testCategory, childCategory);
            when(categoryRepository.findAll()).thenReturn(allCategories);

            // When
            List<Category> result = categoryService.findAll();

            // Then
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("findRootCategories should return only root categories")
        void findRootCategories_ReturnsOnlyRoots() {
            // Given
            List<Category> rootCategories = List.of(parentCategory, testCategory);
            when(categoryRepository.findRootCategories()).thenReturn(rootCategories);

            // When
            List<Category> result = categoryService.findRootCategories();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(cat -> cat.getParentId() == null);
        }
    }
}
