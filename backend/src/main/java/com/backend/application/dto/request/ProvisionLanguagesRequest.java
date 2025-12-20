package com.backend.application.dto.request;

import java.util.List;

import com.backend.domain.enums.Language;

import lombok.Data;

@Data
public class ProvisionLanguagesRequest {
  private List<Language> languages;
}
