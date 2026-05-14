package com.backend.application.service;

import java.util.Optional;

import com.backend.application.dto.delivery.PageDeliveryResponse;
import com.backend.domain.enums.Language;

public interface PageDeliveryService {

  Optional<PageDeliveryResponse> resolvePageForDelivery(String pageType, String pageLabelOrId, String code,
      Long previewPageId, Language lang);
}
