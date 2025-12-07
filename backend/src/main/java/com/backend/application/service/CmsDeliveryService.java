package com.backend.application.service;

import java.util.List;
import java.util.Optional;

import com.backend.domain.enums.Language;
import com.backend.application.dto.delivery.BatchDeliveryResponse;
import com.backend.application.dto.delivery.ComponentDeliveryResponse;

public interface CmsDeliveryService {

  Optional<ComponentDeliveryResponse> getComponentByUid(String uid, Language lang);

  BatchDeliveryResponse getComponentsByUids(List<String> uids, Language lang);

  Language getDefaultLanguage();
}
