package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.backend.application.dto.response.SiteOverviewAppDto.ActionsAppDto;
import com.backend.application.dto.response.SiteOverviewAppDto.ActivityTrendDayAppDto;
import com.backend.domain.entity.Site;
import com.backend.domain.entity.SiteActivity;
import com.backend.domain.enums.ActivityAction;
import com.backend.domain.enums.ActivityEntityType;
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

        org.springframework.data.domain.Page<SiteActivity> page = new org.springframework.data.domain.PageImpl<>(
                List.of(first, second),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")),
                15);
        com.backend.domain.entity.User user = new com.backend.domain.entity.User();
        user.setId(1L);
        user.setEmail("admin@democompany.com");
        user.setFullName("Admin");

        when(siteActivityRepository.findRecentActivities(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);
        when(userRepository.findByIdIn(org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(List.of(user));

        Page<com.backend.application.dto.response.SiteOverviewAppDto.ActivityAppDto> result = siteOverviewService
                .getRecentActivityPage(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));

        assertThat(result.getTotalElements()).isEqualTo(15);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).id()).isEqualTo(15L);
        assertThat(result.getContent().get(0).user()).isNotNull();
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
}
