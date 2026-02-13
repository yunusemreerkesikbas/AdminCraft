package com.backend.application.service;

import java.util.List;
import java.util.Optional;

import com.backend.application.dto.delivery.BatchDeliveryResponse;
import com.backend.application.dto.delivery.ComponentDeliveryResponse;
import com.backend.application.dto.delivery.PageDeliveryResponse;
import com.backend.application.dto.delivery.SiteDeliveryResponse;
import com.backend.domain.enums.Language;

public interface CmsDeliveryService {

  Optional<ComponentDeliveryResponse> getComponentByUid(String uid, Language lang);

  BatchDeliveryResponse getComponentsByUids(List<String> uids, Language lang);

  Optional<PageDeliveryResponse> resolvePageForDelivery(String pageType, String pageLabelOrId, String code,
      Language lang);

  Language getDefaultLanguage();

  Optional<SiteDeliveryResponse> getSiteForDelivery();
}
