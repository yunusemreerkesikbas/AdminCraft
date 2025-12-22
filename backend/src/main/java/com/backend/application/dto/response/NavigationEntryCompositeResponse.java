package com.backend.application.dto.response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.backend.domain.entity.NavigationEntry;
import com.backend.domain.entity.NavigationEntryI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.NavigationItemType;

import lombok.Builder;

@Builder
public record NavigationEntryCompositeResponse(
    Long id,
    String uuid,
    String uid,
    Long nodeId,
    NavigationItemType itemType,
    String itemId,
    String url,
    String linkColor,
    String target,
    Boolean isExternal,
    Integer sortOrder,
    Boolean isVisible,
    Map<Language, NavigationEntryI18nResponse> translations) {

  /**
   * Create a NavigationEntryCompositeResponse from a NavigationEntry and its localized entries.
   *
   * @param entry the source NavigationEntry; if {@code null} the method returns {@code null}
   * @param i18nList the list of NavigationEntryI18n to include as translations (mapped by Language)
   * @return {@code null} if {@code entry} is {@code null}, otherwise a NavigationEntryCompositeResponse
   *         populated with fields from {@code entry} and a translations map constructed from {@code i18nList}
   */
  public static NavigationEntryCompositeResponse from(NavigationEntry entry, List<NavigationEntryI18n> i18nList) {
    if (entry == null) {
      return null;
    }

    Map<Language, NavigationEntryI18nResponse> translationsMap = i18nList.stream()
        .collect(Collectors.toMap(
            NavigationEntryI18n::getLanguage,
            NavigationEntryI18nResponse::from));

    return NavigationEntryCompositeResponse.builder()
        .id(entry.getId())
        .uuid(entry.getUuid())
        .uid(entry.getUid())
        .nodeId(entry.getNodeId())
        .itemType(entry.getItemType())
        .itemId(entry.getItemId())
        .url(entry.getUrl())
        .linkColor(entry.getLinkColor())
        .target(entry.getTarget())
        .isExternal(entry.getIsExternal())
        .sortOrder(entry.getSortOrder())
        .isVisible(entry.getIsVisible())
        .translations(translationsMap)
        .build();
  }
}