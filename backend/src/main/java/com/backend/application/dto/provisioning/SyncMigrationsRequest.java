package com.backend.application.dto.provisioning;

import java.util.List;

import lombok.Data;

@Data
public class SyncMigrationsRequest {

  private List<String> modules;
}
