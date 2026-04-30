package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

import com.backend.application.dto.request.EntryI18nUpdateCommand;
import com.backend.application.dto.request.UpdateComponentEntryCompositeRequest;
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
  private MediaService mediaService;

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
    when(mediaService.findByUids(List.of("homepage-award-1"))).thenReturn(List.of(media));

    ComponentEntryCompositeResponse response = componentEntryService.getEntryWithTranslations(30L);

    Map<String, Object> customFields = response.translations().get(Language.TR).customFields();
    assertThat(customFields).containsEntry("mediaUid", "homepage-award-1");
    assertThat(customFields.get("media")).isInstanceOf(ResponsiveMediaResponse.MediaSummary.class);

    ResponsiveMediaResponse.MediaSummary summary =
        (ResponsiveMediaResponse.MediaSummary) customFields.get("media");
    assertThat(summary.id()).isEqualTo(7L);
    assertThat(summary.publicUrl()).isEqualTo("/api/media/files/award-1.png");
    assertThat(summary.fileSizeFormatted()).isEqualTo("25.7 KB");

    verify(mediaService).findByUids(List.of("homepage-award-1"));
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
    verifyNoInteractions(mediaService);
  }

  @Test
  void updateComposite_ShouldClearTitleWhenExplicitlyProvidedAsEmpty() throws Exception {
    ComponentEntry entry = buildEntry(40L);
    ComponentEntryI18n existingTranslation = buildTranslation(40L, "Existing title", "Existing description",
        "{\"buttonText\":\"Explore\"}");

    when(entryRepository.findById(40L)).thenReturn(Optional.of(entry));
    when(entryRepository.save(entry)).thenReturn(entry);
    when(entryI18nRepository.findByEntryId(40L)).thenReturn(List.of(existingTranslation));
    when(entryI18nRepository.save(any(ComponentEntryI18n.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UpdateComponentEntryCompositeRequest request = new UpdateComponentEntryCompositeRequest(
        null,
        null,
        null,
        null,
        Map.of(Language.EN, updateCommand("{\"title\":\"   \"}")));

    componentEntryService.updateComposite(40L, request);

    assertThat(existingTranslation.getTitle()).isEmpty();
    assertThat(existingTranslation.getDescription()).isEqualTo("Existing description");
    assertThat(existingTranslation.getCustomData()).isEqualTo("{\"buttonText\":\"Explore\"}");
  }

  @Test
  void updateComposite_ShouldPreserveDescriptionWhenMissingFromPayload() throws Exception {
    ComponentEntry entry = buildEntry(41L);
    ComponentEntryI18n existingTranslation = buildTranslation(41L, "Existing title", "Existing description", null);

    when(entryRepository.findById(41L)).thenReturn(Optional.of(entry));
    when(entryRepository.save(entry)).thenReturn(entry);
    when(entryI18nRepository.findByEntryId(41L)).thenReturn(List.of(existingTranslation));
    when(entryI18nRepository.save(any(ComponentEntryI18n.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UpdateComponentEntryCompositeRequest request = new UpdateComponentEntryCompositeRequest(
        null,
        null,
        null,
        null,
        Map.of(Language.EN, updateCommand("{\"title\":\"Updated title\"}")));

    componentEntryService.updateComposite(41L, request);

    assertThat(existingTranslation.getTitle()).isEqualTo("Updated title");
    assertThat(existingTranslation.getDescription()).isEqualTo("Existing description");
  }

  @Test
  void updateComposite_ShouldPreserveCustomDataWhenDynamicFieldsMissing() throws Exception {
    ComponentEntry entry = buildEntry(42L);
    ComponentEntryI18n existingTranslation = buildTranslation(42L, "Existing title", "Existing description",
        "{\"buttonText\":\"Explore\"}");

    when(entryRepository.findById(42L)).thenReturn(Optional.of(entry));
    when(entryRepository.save(entry)).thenReturn(entry);
    when(entryI18nRepository.findByEntryId(42L)).thenReturn(List.of(existingTranslation));
    when(entryI18nRepository.save(any(ComponentEntryI18n.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UpdateComponentEntryCompositeRequest request = new UpdateComponentEntryCompositeRequest(
        null,
        null,
        null,
        null,
        Map.of(Language.EN, updateCommand("{\"description\":\"Updated description\"}")));

    componentEntryService.updateComposite(42L, request);

    assertThat(existingTranslation.getDescription()).isEqualTo("Updated description");
    assertThat(existingTranslation.getCustomData()).isEqualTo("{\"buttonText\":\"Explore\"}");
  }

  @Test
  void updateComposite_ShouldClearCustomDataWhenDynamicFieldsExplicitlyEmpty() throws Exception {
    ComponentEntry entry = buildEntry(43L);
    ComponentEntryI18n existingTranslation = buildTranslation(43L, "Existing title", "Existing description",
        "{\"buttonText\":\"Explore\"}");

    when(entryRepository.findById(43L)).thenReturn(Optional.of(entry));
    when(entryRepository.save(entry)).thenReturn(entry);
    when(entryI18nRepository.findByEntryId(43L)).thenReturn(List.of(existingTranslation));
    when(entryI18nRepository.save(any(ComponentEntryI18n.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UpdateComponentEntryCompositeRequest request = new UpdateComponentEntryCompositeRequest(
        null,
        null,
        null,
        null,
        Map.of(Language.EN, updateCommand("{\"dynamicFields\":{}}")));

    componentEntryService.updateComposite(43L, request);

    assertThat(existingTranslation.getCustomData()).isNull();
  }

  private ComponentEntry buildEntry(Long id) {
    ComponentEntry entry = new ComponentEntry();
    entry.setId(id);
    entry.setComponentId(90L);
    entry.setSortOrder(0);
    entry.setIsVisible(true);
    entry.setStatus(ComponentStatus.DRAFT);
    return entry;
  }

  private ComponentEntryI18n buildTranslation(Long entryId, String title, String description, String customData) {
    ComponentEntryI18n translation = new ComponentEntryI18n();
    translation.setEntryId(entryId);
    translation.setLanguage(Language.EN);
    translation.setTitle(title);
    translation.setDescription(description);
    translation.setCustomData(customData);
    return translation;
  }

  private EntryI18nUpdateCommand updateCommand(String json) throws Exception {
    return new EntryI18nUpdateCommand(objectMapper.readTree(json));
  }
}
