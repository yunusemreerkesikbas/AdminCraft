package com.backend.infrastructure.codec;

import java.util.List;

import org.springframework.stereotype.Component;

import com.backend.application.codec.TemplateAllowedTypesCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    } catch (Exception ex) {
      throw new IllegalArgumentException("Invalid allowedTypes", ex);
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
    } catch (Exception ex) {
      throw new IllegalArgumentException("Invalid allowedTypesJson", ex);
    }
  }
}


