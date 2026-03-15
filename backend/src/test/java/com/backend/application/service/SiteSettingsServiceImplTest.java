package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.application.dto.SiteSettingsAppDto.SiteSettingsAppResponseDto;
import com.backend.domain.entity.Media;
import com.backend.domain.entity.Site;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.SiteRepository;
import com.backend.domain.repository.SiteSettingRepository;
import com.backend.presentation.dto.response.TenantLanguagesResponse;

@ExtendWith(MockitoExtension.class)
class SiteSettingsServiceImplTest {

  @Mock
  private SiteSettingRepository repository;

  @Mock
  private TenantLanguageService tenantLanguageService;

  @Mock
  private SiteRepository siteRepository;

  @Mock
  private MediaService mediaService;

  @InjectMocks
  private SiteSettingsServiceImpl siteSettingsService;

  @Test
  void getAdminSettings_ShouldHydrateLogoMediaSummaries() {
    Site site = new Site();
    site.setLogoMediaUid("logo-light");
    site.setLogoDarkMediaUid("logo-dark");

    Media lightLogo = new Media();
    lightLogo.setId(10L);
    lightLogo.setUid("logo-light");
    lightLogo.setFileName("logo-light.png");
    lightLogo.setOriginalName("logo-light-original.png");
    lightLogo.setFilePath("/uploads/logo-light.png");
    lightLogo.setMimeType("image/png");
    lightLogo.setFileSize(1024L);

    Media darkLogo = new Media();
    darkLogo.setId(11L);
    darkLogo.setUid("logo-dark");
    darkLogo.setFileName("logo-dark.png");
    darkLogo.setOriginalName("logo-dark-original.png");
    darkLogo.setFilePath("/uploads/logo-dark.png");
    darkLogo.setMimeType("image/png");
    darkLogo.setFileSize(2048L);

    when(repository.findByTenantId(1L)).thenReturn(List.of());
    when(siteRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(site));
    when(tenantLanguageService.getLanguages(1L))
        .thenReturn(new TenantLanguagesResponse(1L, Language.EN, Set.of(Language.EN)));
    when(mediaService.findByUids(List.of("logo-light", "logo-dark")))
        .thenReturn(List.of(lightLogo, darkLogo));

    SiteSettingsAppResponseDto response = siteSettingsService.getAdminSettings(1L);

    assertThat(response.global().logoMediaUid()).isEqualTo("logo-light");
    assertThat(response.global().logoMedia()).isNotNull();
    assertThat(response.global().logoMedia().uid()).isEqualTo("logo-light");
    assertThat(response.global().logoMedia().publicUrl()).isEqualTo("/api/media/files/logo-light.png");
    assertThat(response.global().logoDarkMedia()).isNotNull();
    assertThat(response.global().logoDarkMedia().uid()).isEqualTo("logo-dark");
  }

  @Test
  void getAdminSettings_ShouldSkipLogoMediaLookupWhenNoLogoUidsExist() {
    when(repository.findByTenantId(2L)).thenReturn(List.of());
    when(siteRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(new Site()));
    when(tenantLanguageService.getLanguages(2L))
        .thenReturn(new TenantLanguagesResponse(2L, Language.TR, Set.of(Language.TR)));

    SiteSettingsAppResponseDto response = siteSettingsService.getAdminSettings(2L);

    assertThat(response.global().logoMedia()).isNull();
    assertThat(response.global().logoDarkMedia()).isNull();
    verifyNoInteractions(mediaService);
  }
}
