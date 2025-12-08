package com.backend.application.service;

import java.util.List;
import java.util.Optional;

import com.backend.application.dto.delivery.BatchPageDeliveryResponse;
import com.backend.application.dto.delivery.PageDeliveryResponse;
import com.backend.domain.enums.Language;

public interface PageDeliveryService {

  Optional<PageDeliveryResponse> getPageByUid(String uid, Language lang);

  BatchPageDeliveryResponse getPagesByUids(List<String> uids, Language lang);
}

