package com.backend.domain.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModuleCodeTest {

    @Test
    void shouldReturnCorrectCodeForEachModule() {
        assertThat(ModuleCode.CORE.getCode()).isEqualTo("core");
        assertThat(ModuleCode.PAGEBUILDER.getCode()).isEqualTo("pagebuilder");
        assertThat(ModuleCode.PAGE_CATEGORIES.getCode()).isEqualTo("page_categories");
        assertThat(ModuleCode.SITE_SETTINGS.getCode()).isEqualTo("site_settings");
    }

    @Test
    void shouldReturnTurkishDisplayName() {
        assertThat(ModuleCode.CORE.getDisplayNameTr()).isEqualTo("Çekirdek Sistem");
        assertThat(ModuleCode.PAGEBUILDER.getDisplayNameTr()).isEqualTo("Sayfa Oluşturucu");
        assertThat(ModuleCode.PAGE_CATEGORIES.getDisplayNameTr()).isEqualTo("Sayfa Kategorileri");
        assertThat(ModuleCode.SITE_SETTINGS.getDisplayNameTr()).isEqualTo("Site Ayarları");
    }

    @Test
    void shouldReturnEnglishDisplayName() {
        assertThat(ModuleCode.CORE.getDisplayNameEn()).isEqualTo("Core System");
        assertThat(ModuleCode.PAGEBUILDER.getDisplayNameEn()).isEqualTo("Page Builder");
        assertThat(ModuleCode.PAGE_CATEGORIES.getDisplayNameEn()).isEqualTo("Page Categories");
        assertThat(ModuleCode.SITE_SETTINGS.getDisplayNameEn()).isEqualTo("Site Settings");
    }

    @Test
    void shouldReturnDisplayNameBasedOnLanguage() {
        assertThat(ModuleCode.CORE.getDisplayName(Language.TR)).isEqualTo("Çekirdek Sistem");
        assertThat(ModuleCode.CORE.getDisplayName(Language.EN)).isEqualTo("Core System");
        assertThat(ModuleCode.PAGEBUILDER.getDisplayName(Language.TR)).isEqualTo("Sayfa Oluşturucu");
        assertThat(ModuleCode.PAGEBUILDER.getDisplayName(Language.EN)).isEqualTo("Page Builder");
    }

    @Test
    void shouldIndicateCoreAsRequired() {
        assertThat(ModuleCode.CORE.isRequired()).isTrue();
        assertThat(ModuleCode.PAGEBUILDER.isRequired()).isFalse();
        assertThat(ModuleCode.PAGE_CATEGORIES.isRequired()).isFalse();
        assertThat(ModuleCode.SITE_SETTINGS.isRequired()).isFalse();
    }

    @Test
    void shouldFindModuleByCode() {
        assertThat(ModuleCode.fromCode("core")).isEqualTo(ModuleCode.CORE);
        assertThat(ModuleCode.fromCode("pagebuilder")).isEqualTo(ModuleCode.PAGEBUILDER);
        assertThat(ModuleCode.fromCode("page_categories")).isEqualTo(ModuleCode.PAGE_CATEGORIES);
        assertThat(ModuleCode.fromCode("site_settings")).isEqualTo(ModuleCode.SITE_SETTINGS);
    }

    @Test
    void shouldFindModuleByCodeCaseInsensitive() {
        assertThat(ModuleCode.fromCode("CORE")).isEqualTo(ModuleCode.CORE);
        assertThat(ModuleCode.fromCode("PageBuilder")).isEqualTo(ModuleCode.PAGEBUILDER);
        assertThat(ModuleCode.fromCode("PAGE_CATEGORIES")).isEqualTo(ModuleCode.PAGE_CATEGORIES);
        assertThat(ModuleCode.fromCode("Site_Settings")).isEqualTo(ModuleCode.SITE_SETTINGS);
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
        assertThat(ModuleCode.isValidCode("page_categories")).isTrue();
        assertThat(ModuleCode.isValidCode("site_settings")).isTrue();
    }

    @Test
    void shouldValidateValidCodesCaseInsensitive() {
        assertThat(ModuleCode.isValidCode("CORE")).isTrue();
        assertThat(ModuleCode.isValidCode("PageBuilder")).isTrue();
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
        assertThat(allModules).hasSize(4);
        assertThat(allModules).contains(
                ModuleCode.CORE,
                ModuleCode.PAGEBUILDER,
                ModuleCode.PAGE_CATEGORIES,
                ModuleCode.SITE_SETTINGS
        );
    }

    @Test
    void shouldHaveOnlyOneCoreModule() {
        long requiredCount = java.util.Arrays.stream(ModuleCode.values())
                .filter(ModuleCode::isRequired)
                .count();
        assertThat(requiredCount).isEqualTo(1);
    }

    @Test
    void shouldDefaultToTurkishWhenLanguageNotSpecified() {
        // Test the switch default case
        assertThat(ModuleCode.CORE.getDisplayName(Language.TR))
                .isEqualTo(ModuleCode.CORE.getDisplayNameTr());
    }
}
