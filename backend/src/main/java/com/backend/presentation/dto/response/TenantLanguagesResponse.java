package com.backend.presentation.dto.response;

import java.util.List;

public record TenantLanguagesResponse(
    String defaultLanguage,
    List<String> supported) {
}
