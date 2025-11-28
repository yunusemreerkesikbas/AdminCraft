package com.backend.application.dto.provisioning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleCatalogResponse {

  private String code;
  private String name;
  private String type;
  private String version;
  private List<String> deps;
  private Boolean enabledByDefault;
  private String description;
}

