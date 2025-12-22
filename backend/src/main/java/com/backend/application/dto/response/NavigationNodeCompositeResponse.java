package com.backend.application.dto.response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.backend.domain.entity.NavigationNode;
import com.backend.domain.entity.NavigationNodeI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.NodePosition;

import lombok.Builder;

@Builder
public record NavigationNodeCompositeResponse(
    Long id,
    String uuid,
    String uid,
    Long parentId,
    NodePosition position,
    Integer sortOrder,
    Boolean isVisible,
    Boolean isTab,
    Map<Language, NavigationNodeI18nResponse> translations) {

  /**
   * Create a composite response that combines a navigation node's core fields with its localized translations.
   *
   * @param node the source NavigationNode; may be null
   * @param i18nList list of NavigationNodeI18n entries whose Language keys will map to NavigationNodeI18nResponse values
   * @return the populated NavigationNodeCompositeResponse, or `null` if {@code node} is null
   */
  public static NavigationNodeCompositeResponse from(NavigationNode node, List<NavigationNodeI18n> i18nList) {
    if (node == null) {
      return null;
    }

    Map<Language, NavigationNodeI18nResponse> translationsMap = i18nList.stream()
        .collect(Collectors.toMap(
            NavigationNodeI18n::getLanguage,
            NavigationNodeI18nResponse::from));

    return NavigationNodeCompositeResponse.builder()
        .id(node.getId())
        .uuid(node.getUuid())
        .uid(node.getUid())
        .parentId(node.getParentId())
        .position(node.getPosition())
        .sortOrder(node.getSortOrder())
        .isVisible(node.getIsVisible())
        .isTab(node.getIsTab())
        .translations(translationsMap)
        .build();
  }
}