package com.backend.infrastructure.validation;

import java.util.HashMap;
import java.util.Map;

public class ValidationContext {

  private final Map<String, Object> attributes = new HashMap<>();

  public void set(String key, Object value) {
    attributes.put(key, value);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String key) {
    return (T) attributes.get(key);
  }

  public <T> T get(String key, T defaultValue) {
    T value = get(key);
    return value != null ? value : defaultValue;
  }

  public boolean has(String key) {
    return attributes.containsKey(key);
  }

  public static ValidationContext empty() {
    return new ValidationContext();
  }

  public static ValidationContext of(String key, Object value) {
    ValidationContext context = new ValidationContext();
    context.set(key, value);
    return context;
  }
}
