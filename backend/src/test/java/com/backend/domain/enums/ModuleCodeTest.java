package com.backend.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ModuleCodeTest {

    @Test
    void shouldReturnCorrectCodeForEachModule() {
        assertThat(ModuleCode.CORE.getCode()).isEqualTo("core");
        assertThat(ModuleCode.PAGEBUILDER.getCode()).isEqualTo("pagebuilder");
        assertThat(ModuleCode.MEDIA.getCode()).isEqualTo("media");
        assertThat(ModuleCode.COMPONENT_LIBRARY.getCode()).isEqualTo("component_library");
        assertThat(ModuleCode.PRODUCT_CATALOG.getCode()).isEqualTo("product");
    }

    @Test
    void shouldReturnCorrectNameForEachModule() {
        assertThat(ModuleCode.CORE.getName()).isEqualTo("Core");
        assertThat(ModuleCode.PAGEBUILDER.getName()).isEqualTo("Page Builder");
        assertThat(ModuleCode.MEDIA.getName()).isEqualTo("Media Library");
        assertThat(ModuleCode.COMPONENT_LIBRARY.getName()).isEqualTo("Component Library");
        assertThat(ModuleCode.PRODUCT_CATALOG.getName()).isEqualTo("Product Catalog");
    }

    @Test
    void shouldFindModuleByCode() {
        assertThat(ModuleCode.fromCode("core")).isEqualTo(ModuleCode.CORE);
        assertThat(ModuleCode.fromCode("pagebuilder")).isEqualTo(ModuleCode.PAGEBUILDER);
        assertThat(ModuleCode.fromCode("media")).isEqualTo(ModuleCode.MEDIA);
        assertThat(ModuleCode.fromCode("component_library")).isEqualTo(ModuleCode.COMPONENT_LIBRARY);
        assertThat(ModuleCode.fromCode("product")).isEqualTo(ModuleCode.PRODUCT_CATALOG);
    }

    @Test
    void shouldFindModuleByCodeCaseInsensitive() {
        assertThat(ModuleCode.fromCode("CORE")).isEqualTo(ModuleCode.CORE);
        assertThat(ModuleCode.fromCode("PageBuilder")).isEqualTo(ModuleCode.PAGEBUILDER);
        assertThat(ModuleCode.fromCode("MEDIA")).isEqualTo(ModuleCode.MEDIA);
        assertThat(ModuleCode.fromCode("Component_Library")).isEqualTo(ModuleCode.COMPONENT_LIBRARY);
        assertThat(ModuleCode.fromCode("PRODUCT")).isEqualTo(ModuleCode.PRODUCT_CATALOG);
    }

    @Test
    void shouldThrowExceptionForInvalidCode() {
        assertThatThrownBy(() -> ModuleCode.fromCode("invalid_module"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported module code: invalid_module");
    }

    @Test
    void shouldThrowExceptionForNullCode() {
        assertThatThrownBy(() -> ModuleCode.fromCode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported module code: null");
    }

    @Test
    void shouldValidateValidCodes() {
        assertThat(ModuleCode.isValidCode("core")).isTrue();
        assertThat(ModuleCode.isValidCode("pagebuilder")).isTrue();
        assertThat(ModuleCode.isValidCode("media")).isTrue();
        assertThat(ModuleCode.isValidCode("component_library")).isTrue();
        assertThat(ModuleCode.isValidCode("product")).isTrue();
    }

    @Test
    void shouldValidateValidCodesCaseInsensitive() {
        assertThat(ModuleCode.isValidCode("CORE")).isTrue();
        assertThat(ModuleCode.isValidCode("PageBuilder")).isTrue();
        assertThat(ModuleCode.isValidCode("MEDIA")).isTrue();
        assertThat(ModuleCode.isValidCode("Component_Library")).isTrue();
        assertThat(ModuleCode.isValidCode("PRODUCT")).isTrue();
    }

    @Test
    void shouldInvalidateInvalidCodes() {
        assertThat(ModuleCode.isValidCode("invalid")).isFalse();
        assertThat(ModuleCode.isValidCode("unknown_module")).isFalse();
        assertThat(ModuleCode.isValidCode("")).isFalse();
    }

    @Test
    void shouldReturnAllModuleCodes() {
        ModuleCode[] allModules = ModuleCode.values();
        assertThat(allModules).hasSize(5);
        assertThat(allModules).contains(
                ModuleCode.CORE,
                ModuleCode.PAGEBUILDER,
                ModuleCode.MEDIA,
                ModuleCode.COMPONENT_LIBRARY,
                ModuleCode.PRODUCT_CATALOG);
    }
}
