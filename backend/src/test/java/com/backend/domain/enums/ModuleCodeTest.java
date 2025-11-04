package com.backend.domain.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModuleCodeTest {

    @Test
    void shouldReturnCorrectCodeForEachModule() {
        assertThat(ModuleCode.CORE.getCode()).isEqualTo("core");
        assertThat(ModuleCode.PAGEBUILDER.getCode()).isEqualTo("pagebuilder");
        assertThat(ModuleCode.SITE_SETTINGS.getCode()).isEqualTo("site_settings");
        assertThat(ModuleCode.MEDIA.getCode()).isEqualTo("media");
    }

    @Test
    void shouldReturnCorrectNameForEachModule() {
        assertThat(ModuleCode.CORE.getName()).isEqualTo("Core");
        assertThat(ModuleCode.PAGEBUILDER.getName()).isEqualTo("Page Builder");
        assertThat(ModuleCode.SITE_SETTINGS.getName()).isEqualTo("Site Settings");
        assertThat(ModuleCode.MEDIA.getName()).isEqualTo("Media Library");
    }

    @Test
    void shouldFindModuleByCode() {
        assertThat(ModuleCode.fromCode("core")).isEqualTo(ModuleCode.CORE);
        assertThat(ModuleCode.fromCode("pagebuilder")).isEqualTo(ModuleCode.PAGEBUILDER);
        assertThat(ModuleCode.fromCode("site_settings")).isEqualTo(ModuleCode.SITE_SETTINGS);
        assertThat(ModuleCode.fromCode("media")).isEqualTo(ModuleCode.MEDIA);
    }

    @Test
    void shouldFindModuleByCodeCaseInsensitive() {
        assertThat(ModuleCode.fromCode("CORE")).isEqualTo(ModuleCode.CORE);
        assertThat(ModuleCode.fromCode("PageBuilder")).isEqualTo(ModuleCode.PAGEBUILDER);
        assertThat(ModuleCode.fromCode("Site_Settings")).isEqualTo(ModuleCode.SITE_SETTINGS);
        assertThat(ModuleCode.fromCode("MEDIA")).isEqualTo(ModuleCode.MEDIA);
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
        assertThat(ModuleCode.isValidCode("site_settings")).isTrue();
        assertThat(ModuleCode.isValidCode("media")).isTrue();
    }

    @Test
    void shouldValidateValidCodesCaseInsensitive() {
        assertThat(ModuleCode.isValidCode("CORE")).isTrue();
        assertThat(ModuleCode.isValidCode("PageBuilder")).isTrue();
        assertThat(ModuleCode.isValidCode("MEDIA")).isTrue();
    }

    @Test
    void shouldInvalidateInvalidCodes() {
        assertThat(ModuleCode.isValidCode("invalid")).isFalse();
        assertThat(ModuleCode.isValidCode("unknown_module")).isFalse();
        assertThat(ModuleCode.isValidCode("page_categories")).isFalse();
        assertThat(ModuleCode.isValidCode("")).isFalse();
    }

    @Test
    void shouldReturnAllModuleCodes() {
        ModuleCode[] allModules = ModuleCode.values();
        assertThat(allModules).hasSize(4);
        assertThat(allModules).contains(
                ModuleCode.CORE,
                ModuleCode.PAGEBUILDER,
                ModuleCode.SITE_SETTINGS,
                ModuleCode.MEDIA);
    }
}
