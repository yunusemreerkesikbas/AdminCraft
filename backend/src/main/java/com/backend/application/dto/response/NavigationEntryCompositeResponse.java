package com.backend.application.dto.response;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

  public static NavigationEntryCompositeResponse from(NavigationEntry entry, List<NavigationEntryI18n> i18nList) {
    if (entry == null) {
      return null;
    }

    Map<Language, NavigationEntryI18nResponse> translationsMap = Optional.ofNullable(i18nList)
        .orElseGet(Collections::emptyList)
        .stream()
        .collect(Collectors.toMap(
            NavigationEntryI18n::getLanguage,
            NavigationEntryI18nResponse::from,
            (existing, replacement) -> replacement)); // Merge: prefer latest if duplicates

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
