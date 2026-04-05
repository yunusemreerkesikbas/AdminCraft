package com.backend.infrastructure.persistence.platform.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.entity.PlatformDemoRequest;

@Component
public class PlatformDemoRequestMapper {

    public PlatformDemoRequest toDomain(
            com.backend.infrastructure.persistence.platform.entity.PlatformDemoRequest source) {
        if (source == null) {
            return null;
        }
        return PlatformDemoRequest.builder()
                .id(source.getId())
                .uid(source.getUid())
                .fullName(source.getFullName())
                .email(source.getEmail())
                .phone(source.getPhone())
                .message(source.getMessage())
                .locale(source.getLocale())
                .source(source.getSource())
                .clientIp(source.getClientIp())
                .userAgent(source.getUserAgent())
                .createdAt(source.getCreatedAt())
                .build();
    }

    public com.backend.infrastructure.persistence.platform.entity.PlatformDemoRequest toEntity(
            PlatformDemoRequest source) {
        if (source == null) {
            return null;
        }
        com.backend.infrastructure.persistence.platform.entity.PlatformDemoRequest entity =
                new com.backend.infrastructure.persistence.platform.entity.PlatformDemoRequest();
        entity.setFullName(source.getFullName());
        entity.setEmail(source.getEmail());
        entity.setPhone(source.getPhone());
        entity.setMessage(source.getMessage());
        entity.setLocale(source.getLocale());
        entity.setSource(source.getSource() != null ? source.getSource() : "landing");
        entity.setClientIp(source.getClientIp());
        entity.setUserAgent(source.getUserAgent());
        return entity;
    }
}
