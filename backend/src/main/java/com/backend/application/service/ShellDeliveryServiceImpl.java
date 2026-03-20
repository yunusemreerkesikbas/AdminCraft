package com.backend.application.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.delivery.ComponentDeliveryResponse;
import com.backend.application.dto.delivery.EntryDeliveryResponse;
import com.backend.application.dto.delivery.NavigationDeliveryResponse;
import com.backend.application.dto.delivery.ShellDeliveryResponse;
import com.backend.application.dto.delivery.ShellDeliveryResponse.FooterDelivery;
import com.backend.application.dto.delivery.ShellDeliveryResponse.HeaderDelivery;
import com.backend.application.dto.delivery.ShellDeliveryResponse.LayoutBlockDelivery;
import com.backend.application.dto.delivery.LayoutLinkDeliveryDto;
import com.backend.domain.entity.Component;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.ComponentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ShellDeliveryServiceImpl implements ShellDeliveryService {

  private static final String DEFAULT_LANG_CODE = "tr";
  private static final String HEADER_MAIN_NAVIGATION_ROLE = "header.mainNavigation";
  private static final String HEADER_PRIMARY_ROLE_PREFIX = "header.primary.";
  private static final String HEADER_CONTACT_INFO_ROLE = "header.contactInfo";
  private static final String FOOTER_SOCIAL_LINKS_ROLE = "footer.socialLinks";
  private static final String FOOTER_BOTTOM_ROLE_PREFIX = "footer.bottom.";

  private final ComponentRepository componentRepository;
  private final ComponentDeliveryService componentDeliveryService;

  @Override
  public Optional<ShellDeliveryResponse> getShellForDelivery(Language lang) {
    List<Component> shellComponents = componentRepository.findPublishedShellComponents();
    if (shellComponents.isEmpty()) {
      return Optional.empty();
    }

    List<String> uids = shellComponents.stream().map(Component::getUid).toList();
    Map<String, ComponentDeliveryResponse> componentMap =
        componentDeliveryService.getComponentsByUids(uids, lang).data();

    String langCode = lang != null ? lang.getCode() : DEFAULT_LANG_CODE;

    List<LayoutBlockDelivery> headerPrimary = new ArrayList<>();
    List<LayoutBlockDelivery> headerSecondary = new ArrayList<>();
    NavigationDeliveryResponse mainNavigation = null;

    List<LayoutBlockDelivery> footerPrimary = new ArrayList<>();
    List<LayoutBlockDelivery> footerBottom = new ArrayList<>();

    for (Component component : shellComponents) {
      ComponentDeliveryResponse delivery = componentMap.get(component.getUid());
      if (delivery == null || Boolean.FALSE.equals(delivery.isVisible())) {
        continue;
      }

      String role = readCustomString(delivery, "layoutRole");
      if (role == null) {
        log.debug("[Shell] Component {} ({}) has no layoutRole, skipping", delivery.uid(), delivery.type());
        continue;
      }

      if (role.startsWith("header.")) {
        if (HEADER_MAIN_NAVIGATION_ROLE.equals(role)) {
          NavigationDeliveryResponse nav = delivery.navigationNode();
          if (nav != null) {
            List<NavigationDeliveryResponse.NavigationSectionDelivery> sections = buildSections(nav, langCode);
            mainNavigation = NavigationDeliveryResponse.builder()
                .uid(nav.getUid())
                .title(nav.getTitle())
                .position(nav.getPosition())
                .isTab(nav.getIsTab())
                .entries(nav.getEntries())
                .children(nav.getChildren())
                .sections(sections)
                .build();
          }
        } else {
          LayoutBlockDelivery block = toLayoutBlock(delivery, role, langCode);
          if (isPrimaryHeaderRole(role)) {
            headerPrimary.add(block);
          } else {
            headerSecondary.add(block);
          }
        }
      } else if (role.startsWith("footer.")) {
        LayoutBlockDelivery block = toLayoutBlock(delivery, role, langCode);
        if (isBottomFooterRole(role)) {
          footerBottom.add(block);
        } else {
          footerPrimary.add(block);
        }
      }
    }

    HeaderDelivery header = new HeaderDelivery(mainNavigation, headerPrimary, headerSecondary);
    FooterDelivery footer = new FooterDelivery(footerPrimary, footerBottom);

    return Optional.of(new ShellDeliveryResponse(header, footer));
  }

  private LayoutBlockDelivery toLayoutBlock(ComponentDeliveryResponse delivery, String role, String langCode) {
    NavigationDeliveryResponse navNode = delivery.navigationNode();
    List<LayoutLinkDeliveryDto> links;
    if (navNode != null) {
      links = collectLinks(navNode, langCode);
    } else {
      links = buildLinksFromEntries(delivery.entries(), langCode);
    }

    EntryDeliveryResponse firstEntry = delivery.entries() != null && !delivery.entries().isEmpty()
        ? delivery.entries().get(0) : null;

    String newsletterPlaceholder = firstEntry != null ? readEntryString(firstEntry, "inputPlaceholder") : null;
    String newsletterButtonLabel = firstEntry != null ? readEntryString(firstEntry, "buttonLabel") : null;

    return new LayoutBlockDelivery(
        delivery.uid(),
        role,
        delivery.type(),
        nullIfBlank(delivery.title()),
        nullIfBlank(delivery.description()),
        navNode,
        links,
        nullIfBlank(newsletterPlaceholder),
        nullIfBlank(newsletterButtonLabel)
    );
  }

  private List<NavigationDeliveryResponse.NavigationSectionDelivery> buildSections(
      NavigationDeliveryResponse root, String langCode) {
    List<NavigationDeliveryResponse.NavigationSectionDelivery> sections = new ArrayList<>();

    if (root.getEntries() != null) {
      for (NavigationDeliveryResponse.EntryDeliveryDto entry : root.getEntries()) {
        String href = NavigationDeliveryUtils.resolveLocalizedHref(entry.getUrl(), entry.isExternal(), langCode);
        if (href == null) continue;
        String label = entry.getLinkName() != null ? entry.getLinkName() : entry.getUrl();
        sections.add(NavigationDeliveryResponse.NavigationSectionDelivery.builder()
            .uid(entry.getUid())
            .title(label)
            .href(href)
            .isExternal(entry.isExternal())
            .links(List.of())
            .build());
      }
    }

    if (root.getChildren() != null) {
      for (NavigationDeliveryResponse child : root.getChildren()) {
        List<LayoutLinkDeliveryDto> childLinks = collectLinks(child, langCode);
        if (child.getTitle() == null && childLinks.isEmpty()) continue;

        String sectionTitle = child.getTitle() != null
            ? child.getTitle()
            : (!childLinks.isEmpty() ? childLinks.get(0).label() : child.getUid());

        if (childLinks.size() == 1) {
          LayoutLinkDeliveryDto link = childLinks.get(0);
          sections.add(NavigationDeliveryResponse.NavigationSectionDelivery.builder()
              .uid(child.getUid())
              .title(sectionTitle)
              .href(link.href())
              .isExternal(link.isExternal())
              .links(List.of())
              .build());
        } else {
          sections.add(NavigationDeliveryResponse.NavigationSectionDelivery.builder()
              .uid(child.getUid())
              .title(sectionTitle)
              .links(childLinks)
              .build());
        }
      }
    }

    return sections;
  }

  private List<LayoutLinkDeliveryDto> collectLinks(NavigationDeliveryResponse node, String langCode) {
    List<LayoutLinkDeliveryDto> links = new ArrayList<>();
    if (node.getEntries() != null) {
      for (NavigationDeliveryResponse.EntryDeliveryDto entry : node.getEntries()) {
        String href = NavigationDeliveryUtils.resolveLocalizedHref(entry.getUrl(), entry.isExternal(), langCode);
        if (href == null) continue;
        String label = entry.getLinkName() != null ? entry.getLinkName() : entry.getUrl();
        links.add(new LayoutLinkDeliveryDto(entry.getUid(), label, href, entry.isExternal(),
            entry.getTarget(), entry.getLinkColor()));
      }
    }
    if (node.getChildren() != null) {
      for (NavigationDeliveryResponse child : node.getChildren()) {
        links.addAll(collectLinks(child, langCode));
      }
    }
    return dedupeLinks(links);
  }

  private List<LayoutLinkDeliveryDto> buildLinksFromEntries(List<EntryDeliveryResponse> entries, String langCode) {
    if (entries == null) return List.of();
    Map<String, LayoutLinkDeliveryDto> seen = new LinkedHashMap<>();
    for (EntryDeliveryResponse entry : entries) {
      if (Boolean.FALSE.equals(entry.getIsVisible())) continue;

      String label = nullIfBlank(entry.getTitle());
      if (label == null) label = readEntryString(entry, "buttonText");
      if (label == null) label = readEntryString(entry, "label");

      String rawHref = readEntryString(entry, "linkUrl");
      if (rawHref == null) rawHref = readEntryString(entry, "buttonUrl");
      if (rawHref == null) rawHref = readEntryString(entry, "url");

      String href = NavigationDeliveryUtils.resolveLocalizedHref(rawHref, entry.isExternal(), langCode);
      if (label == null || href == null) continue;

      String target = readEntryString(entry, "target");
      if (target == null) {
        Object openInNewTab = entry.getCustomFieldsFlat() != null
            ? entry.getCustomFieldsFlat().get("openInNewTab") : null;
        if (Boolean.TRUE.equals(openInNewTab)) target = "_blank";
      }

      String color = readEntryString(entry, "linkColor");
      String dedupKey = label + "|" + href + "|" + (target != null ? target : "") + "|" + (color != null ? color : "");
      seen.putIfAbsent(dedupKey, new LayoutLinkDeliveryDto(entry.getUid(), label, href, entry.isExternal(), target, color));
    }
    return new ArrayList<>(seen.values());
  }

  private List<LayoutLinkDeliveryDto> dedupeLinks(List<LayoutLinkDeliveryDto> links) {
    Set<String> seen = new HashSet<>();
    List<LayoutLinkDeliveryDto> result = new ArrayList<>();
    for (LayoutLinkDeliveryDto link : links) {
      String key = link.label() + "|" + link.href() + "|" + (link.target() != null ? link.target() : "");
      if (seen.add(key)) result.add(link);
    }
    return result;
  }

  private String readCustomString(ComponentDeliveryResponse delivery, String key) {
    if (delivery.customFields() == null) return null;
    Object val = delivery.customFields().get(key);
    return val instanceof String s ? nullIfBlank(s) : null;
  }

  private String readEntryString(EntryDeliveryResponse entry, String key) {
    if (entry.getCustomFieldsFlat() == null) return null;
    Object val = entry.getCustomFieldsFlat().get(key);
    return val instanceof String s ? nullIfBlank(s) : null;
  }

  private String nullIfBlank(String s) {
    return (s == null || s.isBlank()) ? null : s;
  }

  private boolean isPrimaryHeaderRole(String role) {
    return HEADER_CONTACT_INFO_ROLE.equals(role) || role.startsWith(HEADER_PRIMARY_ROLE_PREFIX);
  }

  private boolean isBottomFooterRole(String role) {
    return FOOTER_SOCIAL_LINKS_ROLE.equals(role) || role.startsWith(FOOTER_BOTTOM_ROLE_PREFIX);
  }
}
