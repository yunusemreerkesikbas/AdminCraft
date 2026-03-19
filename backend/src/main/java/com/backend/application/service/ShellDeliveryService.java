package com.backend.application.service;

import java.util.Optional;

import com.backend.application.dto.delivery.ShellDeliveryResponse;
import com.backend.domain.enums.Language;

public interface ShellDeliveryService {
  Optional<ShellDeliveryResponse> getShellForDelivery(Language lang);
}
