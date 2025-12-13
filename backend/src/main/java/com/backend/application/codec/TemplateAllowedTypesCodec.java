package com.backend.application.codec;

import java.util.List;

public interface TemplateAllowedTypesCodec {
  String encode(List<String> allowedTypes);

  List<String> decode(String allowedTypesJson);
}
