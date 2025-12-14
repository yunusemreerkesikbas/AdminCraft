package com.backend.infrastructure.codec;

import java.util.List;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.backend.application.codec.TemplateAllowedTypesCodec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JacksonTemplateAllowedTypesCodec implements TemplateAllowedTypesCodec {

  private final ObjectMapper objectMapper;

  public JacksonTemplateAllowedTypesCodec(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public String encode(List<String> allowedTypes) {
    if (allowedTypes == null) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(allowedTypes);
    } catch (JsonProcessingException ex) {
      log.error("Failed to encode allowedTypes. tenantId={}, correlationId={}, error={}",
          MDC.get("tenantId"), MDC.get("correlationId"), ex.getMessage());
      throw new IllegalArgumentException("Failed to encode allowedTypes: " + ex.getMessage(), ex);
    }
  }

  @Override
  public List<String> decode(String allowedTypesJson) {
    if (allowedTypesJson == null || allowedTypesJson.isBlank()) {
      return null;
    }
    try {
      return objectMapper.readValue(allowedTypesJson, new TypeReference<List<String>>() {
      });
    } catch (JsonProcessingException ex) {
      log.error("Failed to decode allowedTypesJson. tenantId={}, correlationId={}, json={}, error={}",
          MDC.get("tenantId"), MDC.get("correlationId"), allowedTypesJson, ex.getMessage());
      throw new IllegalArgumentException("Failed to decode allowedTypesJson: " + ex.getMessage(), ex);
    }
  }
}
