package com.backend.presentation.dto.response.delivery;

import java.util.List;
import java.util.Map;

import lombok.Builder;

@Builder
public record BatchDeliveryResponse(
    Map<String, ComponentDeliveryResponse> data,
    BatchMeta meta) {
  @Builder
  public record BatchMeta(
      int requested,
      int found,
      List<String> notFound) {
  }
}
