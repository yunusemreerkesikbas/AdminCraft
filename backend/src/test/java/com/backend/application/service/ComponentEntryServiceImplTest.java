package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.backend.application.dto.response.ComponentEntryCompositeResponse;
import com.backend.application.dto.response.ResponsiveMediaResponse;
import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.entity.Media;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.ComponentEntryI18nRepository;
import com.backend.domain.repository.ComponentEntryRepository;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.MediaRepository;
import com.backend.domain.repository.ResponsiveMediaSetRepository;

@ExtendWith(MockitoExtension.class)
class ComponentEntryServiceImplTest {

  @Mock
  private ComponentEntryRepository entryRepository;

  @Mock
  private ComponentEntryI18nRepository entryI18nRepository;

  @Mock
  private ComponentRepository componentRepository;

  @Mock
  private ResponsiveMediaSetRepository responsiveMediaSetRepository;

  @Mock
  private MediaRepository mediaRepository;

  @Mock
  private ComponentMediaLinkSyncService componentMediaLinkSyncService;

  @Spy
  private ObjectMapper objectMapper = new ObjectMapper();

  @InjectMocks
  private ComponentEntryServiceImpl componentEntryService;

  @Test
  void getEntryWithTranslations_ShouldHydrateLegacyMediaSummary() {
    ComponentEntry entry = new ComponentEntry();
    entry.setId(30L);
    entry.setComponentId(39L);
    entry.setStatus(ComponentStatus.PUBLISHED);

    ComponentEntryI18n translation = new ComponentEntryI18n();
    translation.setEntryId(30L);
    translation.setLanguage(Language.TR);
    translation.setCustomData("{\"mediaUid\":\"homepage-award-1\",\"buttonText\":\"See Our Awards\"}");

    Media media = new Media();
    media.setId(7L);
    media.setUid("homepage-award-1");
    media.setFileName("award-1.png");
    media.setOriginalName("award-1-original.png");
    media.setFilePath("/uploads/award-1.png");
    media.setMimeType("image/png");
    media.setFileSize(26340L);
    media.setWidth(533);
    media.setHeight(650);

    when(entryRepository.findById(30L)).thenReturn(Optional.of(entry));
    when(entryI18nRepository.findByEntryId(30L)).thenReturn(List.of(translation));
    when(mediaRepository.findByUidIn(List.of("homepage-award-1"))).thenReturn(List.of(media));

    ComponentEntryCompositeResponse response = componentEntryService.getEntryWithTranslations(30L);

    Map<String, Object> customFields = response.translations().get(Language.TR).customFields();
    assertThat(customFields).containsEntry("mediaUid", "homepage-award-1");
    assertThat(customFields.get("media")).isInstanceOf(ResponsiveMediaResponse.MediaSummary.class);

    ResponsiveMediaResponse.MediaSummary summary =
        (ResponsiveMediaResponse.MediaSummary) customFields.get("media");
    assertThat(summary.id()).isEqualTo(7L);
    assertThat(summary.publicUrl()).isEqualTo("/api/media/files/award-1.png");
    assertThat(summary.fileSizeFormatted()).isEqualTo("25.7 KB");

    verify(mediaRepository).findByUidIn(List.of("homepage-award-1"));
  }

  @Test
  void getEntryWithTranslations_ShouldSkipMediaLookupWhenLegacyMediaUidMissing() {
    ComponentEntry entry = new ComponentEntry();
    entry.setId(31L);
    entry.setComponentId(39L);

    ComponentEntryI18n translation = new ComponentEntryI18n();
    translation.setEntryId(31L);
    translation.setLanguage(Language.EN);
    translation.setCustomData("{\"buttonText\":\"No media here\"}");

    when(entryRepository.findById(31L)).thenReturn(Optional.of(entry));
    when(entryI18nRepository.findByEntryId(31L)).thenReturn(List.of(translation));

    ComponentEntryCompositeResponse response = componentEntryService.getEntryWithTranslations(31L);

    assertThat(response.translations().get(Language.EN).customFields())
        .containsEntry("buttonText", "No media here")
        .doesNotContainKey("media");
    verifyNoInteractions(mediaRepository);
  }
}
