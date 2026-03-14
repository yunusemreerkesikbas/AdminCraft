package com.backend.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.delivery.BatchDeliveryResponse;
import com.backend.application.dto.delivery.ComponentDeliveryResponse;
import com.backend.application.dto.delivery.PageDeliveryResponse;
import com.backend.application.dto.delivery.SiteDeliveryResponse;
import com.backend.domain.enums.Language;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.SiteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
public class CmsDeliveryServiceImpl implements CmsDeliveryService {

  private final ComponentDeliveryService componentDeliveryService;
  private final PageDeliveryService pageDeliveryService;
  private final TenantContextPort tenantContext;
  private final SiteRepository siteRepository;
  private final MediaService mediaService;

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
    return siteRepository.findFirstByOrderByIdAsc().map(site -> {
      String logoUrl = mediaService.resolvePublicUrl(site.getLogoMediaUid());
      String logoDarkUrl = mediaService.resolvePublicUrl(site.getLogoDarkMediaUid());
      return SiteDeliveryResponse.from(site, logoUrl, logoDarkUrl);
    });
  }

  @Override
  public Language getDefaultLanguage() {
    return tenantContext.getDefaultLanguage();
  }
}
