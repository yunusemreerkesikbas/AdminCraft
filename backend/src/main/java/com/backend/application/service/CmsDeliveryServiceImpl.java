package com.backend.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.delivery.BatchDeliveryResponse;
import com.backend.application.dto.delivery.ComponentDeliveryResponse;
import com.backend.application.dto.delivery.PageDeliveryResponse;
import com.backend.application.dto.delivery.SiteDeliveryResponse;
import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.Language;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.SiteRepository;
import com.backend.domain.repository.TenantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CmsDeliveryServiceImpl implements CmsDeliveryService {

  private final ComponentDeliveryService componentDeliveryService;
  private final PageDeliveryService pageDeliveryService;
  private final TenantContextPort tenantContext;
  private final TenantRepository tenantRepository;
  private final SiteRepository siteRepository;

  @Override
  public Optional<ComponentDeliveryResponse> getComponentByUid(String uid, Language lang) {
    Language resolvedLang = lang != null ? lang : getDefaultLanguage();
    return componentDeliveryService.getComponentByUid(uid, resolvedLang);
  }

  @Override
  public BatchDeliveryResponse getComponentsByUids(List<String> uids, Language lang) {
    Language resolvedLang = lang != null ? lang : getDefaultLanguage();
    return componentDeliveryService.getComponentsByUids(uids, resolvedLang);
  }

  @Override
  public Optional<PageDeliveryResponse> resolvePageForDelivery(String pageType, String pageLabelOrId, String code,
      Language lang) {
    Language resolvedLang = lang != null ? lang : getDefaultLanguage();
    return pageDeliveryService.resolvePageForDelivery(pageType, pageLabelOrId, code, resolvedLang);
  }

  @Override
  public Optional<SiteDeliveryResponse> getSiteForDelivery() {
    return siteRepository.findFirstByOrderByIdAsc()
        .map(SiteDeliveryResponse::from);
  }

  @Override
  public Language getDefaultLanguage() {
    String tenantIdStr = tenantContext.getTenantId();
    if (tenantIdStr == null) {
      return Language.TR;
    }

    try {
      Long tenantId = Long.parseLong(tenantIdStr);
      return tenantRepository.findById(tenantId)
          .map(Tenant::getDefaultLanguage)
          .orElse(Language.TR);
    } catch (NumberFormatException e) {
      log.warn("Invalid tenant ID format: {}", tenantIdStr);
      return Language.TR;
    }
  }
}
