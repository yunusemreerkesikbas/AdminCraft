package com.backend.application.service;

import java.util.List;
import java.util.Optional;

import com.backend.application.dto.delivery.BatchDeliveryResponse;
import com.backend.application.dto.delivery.ComponentDeliveryResponse;
import com.backend.domain.enums.Language;

public interface ComponentDeliveryService {

  Optional<ComponentDeliveryResponse> getComponentByUid(String uid, Language lang);

  BatchDeliveryResponse getComponentsByUids(List<String> uids, Language lang);
}

