package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import com.backend.application.dto.response.SiteOverviewAppDto.ActionsAppDto;
import com.backend.domain.entity.Site;
import com.backend.domain.enums.ModuleCode;
import com.backend.domain.port.FrontendConfigPort;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.MediaRepository;
import com.backend.domain.repository.PageRepository;
import com.backend.domain.repository.ProductRepository;
import com.backend.domain.repository.SiteActivityRepository;
import com.backend.domain.repository.SiteRepository;
import com.backend.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("SiteOverviewServiceImpl Tests")
class SiteOverviewServiceImplTest {

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private SiteActivityRepository siteActivityRepository;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private ComponentRepository componentRepository;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FrontendConfigPort frontendConfig;

    @Mock
    private TenantModuleAccessService tenantModuleAccessService;

    @InjectMocks
    private SiteOverviewServiceImpl siteOverviewService;

    @Test
    @DisplayName("getAvailableActions should use custom domain directly for preview URL")
    void getAvailableActions_ShouldUseCustomDomainDirectly() {
        Site site = new Site();
        site.setCustomDomain("www.acme.com");
        site.setDomain("acme.craftive.io");
        site.setSslEnabled(true);

        when(siteRepository.findAllWithEnabledLanguages()).thenReturn(List.of(site));

        ActionsAppDto actions = siteOverviewService.getAvailableActions();

        assertThat(actions.previewUrl()).isEqualTo("https://www.acme.com?preview=true");
    }

    @Test
    @DisplayName("getAvailableActions should not duplicate platform domain when site domain is already full")
    void getAvailableActions_ShouldNotDuplicatePlatformDomain() {
        Site site = new Site();
        site.setDomain("acme.craftive.io");
        site.setSslEnabled(true);

        when(siteRepository.findAllWithEnabledLanguages()).thenReturn(List.of(site));

        ActionsAppDto actions = siteOverviewService.getAvailableActions();

        assertThat(actions.previewUrl()).isEqualTo("https://acme.craftive.io?preview=true");
    }

    @Test
    @DisplayName("getAvailableActions should fall back to preview subdomain when site domain is missing")
    void getAvailableActions_ShouldUsePreviewFallback() {
        Site site = new Site();
        site.setSslEnabled(false);
        when(frontendConfig.getBaseUrl()).thenReturn("https://s1-%s.craftive.io");
        when(siteRepository.findAllWithEnabledLanguages()).thenReturn(List.of(site));

        ActionsAppDto actions = siteOverviewService.getAvailableActions();

        assertThat(actions.previewUrl()).isEqualTo("http://s1-preview.craftive.io?preview=true");
    }

    @Test
    @DisplayName("getStats should omit product stats when product module is disabled")
    void getStats_ShouldOmitProductStats_WhenProductModuleDisabled() {
	when(tenantModuleAccessService.isEnabledForCurrentTenant(ModuleCode.PRODUCT_CATALOG)).thenReturn(false);

	var stats = siteOverviewService.getStats();

	assertThat(stats.products()).isNull();
	verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("getStats should include product stats when product module is enabled")
    void getStats_ShouldIncludeProductStats_WhenProductModuleEnabled() {
	when(tenantModuleAccessService.isEnabledForCurrentTenant(ModuleCode.PRODUCT_CATALOG)).thenReturn(true);
	when(productRepository.count()).thenReturn(12L);
	when(productRepository.countByStatus(com.backend.domain.enums.ProductStatus.PUBLISHED)).thenReturn(8L);
	when(productRepository.countByStatus(com.backend.domain.enums.ProductStatus.DRAFT)).thenReturn(3L);
	when(productRepository.countByCreatedAtAfter(org.mockito.ArgumentMatchers.any())).thenReturn(2L);

	var stats = siteOverviewService.getStats();

	assertThat(stats.products()).isNotNull();
	assertThat(stats.products().total()).isEqualTo(12L);
	assertThat(stats.products().published()).isEqualTo(8L);
	assertThat(stats.products().draft()).isEqualTo(3L);
	assertThat(stats.products().weeklyChange()).isEqualTo(2L);
    }
}
