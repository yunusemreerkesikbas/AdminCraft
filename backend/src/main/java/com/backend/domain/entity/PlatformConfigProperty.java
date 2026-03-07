package com.backend.domain.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PlatformConfigProperty {

    private Long id;

    private String configKey;

    private String configValue;

    private Boolean secret = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long updatedBy;
}

