package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.backend.application.dto.response.SiteOverviewAppDto.ActionsAppDto;
import com.backend.application.dto.response.SiteOverviewAppDto.ActivityTrendDayAppDto;
import com.backend.domain.entity.Site;
import com.backend.domain.entity.SiteActivity;
import com.backend.domain.entity.SiteTechnicalSettings;
import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.ActivityAction;
import com.backend.domain.enums.ActivityEntityType;
import com.backend.domain.enums.ModuleCode;
import com.backend.domain.enums.TwoFactorPolicy;
import com.backend.domain.port.FrontendConfigPort;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.MediaRepository;
import com.backend.domain.repository.PageRepository;
import com.backend.domain.repository.ProductRepository;
import com.backend.domain.repository.SiteActivityRepository;
import com.backend.domain.repository.SiteRepository;
import com.backend.domain.repository.SiteTechnicalSettingsRepository;
import com.backend.domain.repository.TenantRepository;
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

    @Mock
    private SiteTechnicalSettingsRepository siteTechnicalSettingsRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantContextPort tenantContext;

    @InjectMocks
    private SiteOverviewServiceImpl siteOverviewService;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        lenient().when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
                .thenAnswer(invocation -> {
                    Object[] args = invocation.getArgument(1, Object[].class);
                    String defaultMessage = invocation.getArgument(2, String.class);
                    if (defaultMessage != null && !defaultMessage.isBlank()) {
                        return format(defaultMessage, args);
                    }
                    return format(invocation.getArgument(0, String.class), args);
                });
        lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenAnswer(invocation -> format(invocation.getArgument(0, String.class), invocation.getArgument(1, Object[].class)));
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    @DisplayName("getAvailableActions should use custom domain host root for preview URL")
    void getAvailableActions_ShouldUseCustomDomainDirectly() {
        Site site = new Site();
        site.setCustomDomain("www.acme.com");
        site.setDomain("acme.craftive.io");
        site.setSslEnabled(true);

        when(siteRepository.findAllWithEnabledLanguages()).thenReturn(List.of(site));

        ActionsAppDto actions = siteOverviewService.getAvailableActions();

        assertThat(actions.previewUrl()).isEqualTo("https://www.acme.com");
    }

    @Test
    @DisplayName("getAvailableActions should not duplicate platform domain when site domain is already full")
    void getAvailableActions_ShouldNotDuplicatePlatformDomain() {
        Site site = new Site();
        site.setDomain("acme.craftive.io");
        site.setSslEnabled(true);

        when(siteRepository.findAllWithEnabledLanguages()).thenReturn(List.of(site));

        ActionsAppDto actions = siteOverviewService.getAvailableActions();

        assertThat(actions.previewUrl()).isEqualTo("https://acme.craftive.io");
    }

    @Test
    @DisplayName("getAvailableActions should fall back to preview subdomain when site domain is missing")
    void getAvailableActions_ShouldUsePreviewFallback() {
        Site site = new Site();
        site.setSslEnabled(false);
        when(frontendConfig.getBaseUrl()).thenReturn("https://s1-%s.craftive.io");
        when(siteRepository.findAllWithEnabledLanguages()).thenReturn(List.of(site));

        ActionsAppDto actions = siteOverviewService.getAvailableActions();

        assertThat(actions.previewUrl()).isEqualTo("http://s1-preview.craftive.io");
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

    @Test
    @DisplayName("getActivityTrend should return newest slice first for default sort")
    void getActivityTrend_ShouldReturnNewestSliceFirst() {
        LocalDateTime now = LocalDateTime.now();
        when(siteActivityRepository.findByCreatedAtBetween(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(
                        activity(ActivityAction.UPDATED, now.minusDays(1)),
                        activity(ActivityAction.CREATED, now.minusDays(2)),
                        activity(ActivityAction.PUBLISHED, now.minusDays(3))));

        Page<ActivityTrendDayAppDto> page = siteOverviewService.getActivityTrend(
                PageRequest.of(0, 7, Sort.by(Sort.Direction.DESC, "date")),
                30);

        assertThat(page.getTotalElements()).isEqualTo(30);
        assertThat(page.getContent()).hasSize(7);
        assertThat(LocalDate.parse(page.getContent().get(0).date()))
                .isAfter(LocalDate.parse(page.getContent().get(6).date()));
        assertThat(page.getContent().get(1).updated()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("getActivityTrend should page older slices when moving forward")
    void getActivityTrend_ShouldReturnOlderSlicesOnNextPage() {
        when(siteActivityRepository.findByCreatedAtBetween(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        Page<ActivityTrendDayAppDto> firstPage = siteOverviewService.getActivityTrend(
                PageRequest.of(0, 7, Sort.by(Sort.Direction.DESC, "date")),
                30);
        Page<ActivityTrendDayAppDto> secondPage = siteOverviewService.getActivityTrend(
                PageRequest.of(1, 7, Sort.by(Sort.Direction.DESC, "date")),
                30);

        assertThat(firstPage.getContent()).hasSize(7);
        assertThat(secondPage.getContent()).hasSize(7);
        assertThat(LocalDate.parse(firstPage.getContent().get(6).date()))
                .isAfter(LocalDate.parse(secondPage.getContent().get(0).date()));
    }

    @Test
    @DisplayName("getRecentActivityPage should return paged activity feed")
    void getRecentActivityPage_ShouldReturnPagedActivityFeed() {
        LocalDateTime now = LocalDateTime.now();
        SiteActivity first = activity(ActivityAction.UPDATED, now.minusMinutes(1));
        first.setId(15L);
        first.setUserId(1L);
        SiteActivity second = activity(ActivityAction.CREATED, now.minusMinutes(2));
        second.setId(14L);
        second.setUserId(1L);

        com.backend.domain.entity.User user = new com.backend.domain.entity.User();
        user.setId(1L);
        user.setEmail("admin@democompany.com");
        user.setFullName("Admin");

        when(siteActivityRepository.findRecentActivities(0, 10))
                .thenReturn(List.of(first, second));
        when(siteActivityRepository.count())
                .thenReturn(15L);
        when(userRepository.findByIdIn(org.mockito.ArgumentMatchers.<Long>anyList()))
                .thenReturn(List.of(user));

        Page<com.backend.application.dto.response.SiteOverviewAppDto.ActivityAppDto> result = siteOverviewService
                .getRecentActivityPage(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));

        assertThat(result.getTotalElements()).isEqualTo(15);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).id()).isEqualTo(15L);
        assertThat(result.getContent().get(0).user()).isNotNull();
    }

    @Test
    @DisplayName("getOverview should build healthy spotlight from backend data")
    void getOverview_ShouldBuildHealthySpotlight() {
        Site site = site(1L, true, false, LocalDateTime.of(2026, 4, 1, 10, 0));
        Tenant tenant = tenant(TwoFactorPolicy.REQUIRED);

        when(siteRepository.findAllWithEnabledLanguages()).thenReturn(List.of(site));
        when(pageRepository.count()).thenReturn(5L);
        when(pageRepository.countByStatus(com.backend.domain.enums.PageStatus.PUBLISHED)).thenReturn(5L);
        when(pageRepository.countByStatus(com.backend.domain.enums.PageStatus.DRAFT)).thenReturn(0L);
        when(siteTechnicalSettingsRepository.findBySiteId(1L))
                .thenReturn(java.util.Optional.of(technicalSettings(true, true, true)));
        when(tenantContext.getTenantId()).thenReturn("7");
        when(tenantRepository.findById(7L)).thenReturn(java.util.Optional.of(tenant));
        when(siteActivityRepository.findRecentActivities(any(Integer.class))).thenReturn(List.of());

        var overview = siteOverviewService.getOverview();

        assertThat(overview.spotlight()).isNotNull();
        assertThat(overview.spotlight().operationalScore()).isEqualTo(100);
        assertThat(overview.spotlight().status().tone()).isEqualTo("PRIMARY");
        assertThat(overview.spotlight().status().code()).isEqualTo("healthy");
        assertThat(overview.spotlight().contextCards())
                .extracting(card -> card.valueCode())
                .containsExactly("published", "indexable", "twoFactorRequired");
        assertThat(overview.spotlight().recommendations())
                .extracting(recommendation -> recommendation.id())
                .containsExactly("healthy");
    }

    @Test
    @DisplayName("getOverview should prioritize maintenance and indexing recommendations when critical")
    void getOverview_ShouldBuildCriticalSpotlight() {
        Site site = site(1L, false, true, LocalDateTime.of(2026, 4, 1, 10, 0));
        Tenant tenant = tenant(TwoFactorPolicy.DISABLED);

        when(siteRepository.findAllWithEnabledLanguages()).thenReturn(List.of(site));
        when(pageRepository.count()).thenReturn(5L);
        when(pageRepository.countByStatus(com.backend.domain.enums.PageStatus.PUBLISHED)).thenReturn(2L);
        when(pageRepository.countByStatus(com.backend.domain.enums.PageStatus.DRAFT)).thenReturn(3L);
        when(siteTechnicalSettingsRepository.findBySiteId(1L))
                .thenReturn(java.util.Optional.of(technicalSettings(false, false, false)));
        when(tenantContext.getTenantId()).thenReturn("7");
        when(tenantRepository.findById(7L)).thenReturn(java.util.Optional.of(tenant));
        when(siteActivityRepository.findRecentActivities(any(Integer.class))).thenReturn(List.of());

        var overview = siteOverviewService.getOverview();

        assertThat(overview.spotlight()).isNotNull();
        assertThat(overview.spotlight().status().tone()).isEqualTo("CRITICAL");
        assertThat(overview.spotlight().status().code()).isEqualTo("critical");
        assertThat(overview.spotlight().operationalScore()).isEqualTo(28);
        assertThat(overview.spotlight().recommendations())
                .extracting(recommendation -> recommendation.id())
                .containsExactly("maintenance", "indexing");
    }

    @Test
    @DisplayName("getOverview should include publish pages and 2FA recommendations when they are the top issues")
    void getOverview_ShouldBuildWarningSpotlightRecommendations() {
        Site site = site(1L, true, false, LocalDateTime.of(2026, 4, 1, 10, 0));
        Tenant tenant = tenant(TwoFactorPolicy.DISABLED);

        when(siteRepository.findAllWithEnabledLanguages()).thenReturn(List.of(site));
        when(pageRepository.count()).thenReturn(8L);
        when(pageRepository.countByStatus(com.backend.domain.enums.PageStatus.PUBLISHED)).thenReturn(5L);
        when(pageRepository.countByStatus(com.backend.domain.enums.PageStatus.DRAFT)).thenReturn(3L);
        when(siteTechnicalSettingsRepository.findBySiteId(1L))
                .thenReturn(java.util.Optional.of(technicalSettings(true, true, true)));
        when(tenantContext.getTenantId()).thenReturn("7");
        when(tenantRepository.findById(7L)).thenReturn(java.util.Optional.of(tenant));
        when(siteActivityRepository.findRecentActivities(any(Integer.class))).thenReturn(List.of());

        var overview = siteOverviewService.getOverview();

        assertThat(overview.spotlight()).isNotNull();
        assertThat(overview.spotlight().status().tone()).isEqualTo("WARNING");
        assertThat(overview.spotlight().status().code()).isEqualTo("attention");
        assertThat(overview.spotlight().recommendations())
                .extracting(recommendation -> recommendation.id())
                .containsExactly("publish-pages", "two-factor");
        assertThat(overview.spotlight().recommendations().get(0).count()).isEqualTo(3L);
    }

    @Test
    @DisplayName("getOverview should return null spotlight when site is missing")
    void getOverview_ShouldReturnNullSpotlight_WhenSiteIsMissing() {
        when(siteRepository.findAllWithEnabledLanguages()).thenReturn(List.of());

        var overview = siteOverviewService.getOverview();

        assertThat(overview.spotlight()).isNull();
    }

    @Test
    @DisplayName("getOverview should default to all-green spotlight when technicalSettings are absent")
    void getOverview_ShouldDefaultToGreenSpotlight_WhenTechnicalSettingsAbsent() {
        Site site = site(1L, true, false, LocalDateTime.of(2026, 4, 1, 10, 0));
        Tenant tenant = tenant(TwoFactorPolicy.REQUIRED);

        when(siteRepository.findAllWithEnabledLanguages()).thenReturn(List.of(site));
        when(pageRepository.count()).thenReturn(3L);
        when(pageRepository.countByStatus(com.backend.domain.enums.PageStatus.PUBLISHED)).thenReturn(3L);
        when(pageRepository.countByStatus(com.backend.domain.enums.PageStatus.DRAFT)).thenReturn(0L);
        when(siteTechnicalSettingsRepository.findBySiteId(1L)).thenReturn(java.util.Optional.empty());
        when(tenantContext.getTenantId()).thenReturn("7");
        when(tenantRepository.findById(7L)).thenReturn(java.util.Optional.of(tenant));
        when(siteActivityRepository.findRecentActivities(any(Integer.class))).thenReturn(List.of());

        var overview = siteOverviewService.getOverview();

        assertThat(overview.spotlight()).isNotNull();
        assertThat(overview.spotlight().contextCards())
                .extracting(card -> card.valueCode())
                .contains("indexable");
    }

    @Test
    @DisplayName("getOverview should not throw when tenantId is absent from context")
    void getOverview_ShouldNotThrow_WhenTenantIdIsAbsent() {
        Site site = site(1L, true, false, LocalDateTime.of(2026, 4, 1, 10, 0));

        when(siteRepository.findAllWithEnabledLanguages()).thenReturn(List.of(site));
        when(pageRepository.count()).thenReturn(1L);
        when(pageRepository.countByStatus(com.backend.domain.enums.PageStatus.PUBLISHED)).thenReturn(1L);
        when(pageRepository.countByStatus(com.backend.domain.enums.PageStatus.DRAFT)).thenReturn(0L);
        when(siteTechnicalSettingsRepository.findBySiteId(1L))
                .thenReturn(java.util.Optional.of(technicalSettings(true, true, true)));
        when(tenantContext.getTenantId()).thenReturn(null);
        when(siteActivityRepository.findRecentActivities(any(Integer.class))).thenReturn(List.of());

        var overview = siteOverviewService.getOverview();

        assertThat(overview.spotlight()).isNotNull();
        assertThat(overview.spotlight().status()).isNotNull();
    }

    private SiteActivity activity(ActivityAction action, LocalDateTime createdAt) {
        SiteActivity activity = new SiteActivity();
        activity.setAction(action);
        activity.setEntityType(ActivityEntityType.PAGE);
        activity.setEntityName("Homepage");
        activity.setCreatedAt(createdAt);
        activity.setUpdatedAt(createdAt);
        return activity;
    }

    private Site site(Long id, boolean published, boolean maintenance, LocalDateTime updatedAt) {
        Site site = new Site();
        site.setId(id);
        site.setDomain("demo.craftive.io");
        site.setSslEnabled(true);
        site.setPublished(published);
        site.setMaintenanceMode(maintenance);
        site.setUpdatedAt(updatedAt);
        return site;
    }

    private Tenant tenant(TwoFactorPolicy policy) {
        Tenant tenant = new Tenant();
        tenant.setId(7L);
        tenant.setTwoFactorPolicy(policy);
        return tenant;
    }

    private SiteTechnicalSettings technicalSettings(
            boolean indexingEnabled,
            boolean sitemapEnabled,
            boolean cookieConsentEnabled) {
        return SiteTechnicalSettings.builder()
                .siteId(1L)
                .indexingEnabled(indexingEnabled)
                .sitemapEnabled(sitemapEnabled)
                .cookieConsentEnabled(cookieConsentEnabled)
                .build();
    }

    private String format(String pattern, Object[] args) {
        String result = pattern;
        if (args == null) {
            return result;
        }

        for (int i = 0; i < args.length; i++) {
            result = result.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return result;
    }
}
