package com.backend.domain.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformDemoRequest {

    private Long id;
    private String uid;
    private String fullName;
    private String email;
    private String phone;
    private String message;
    private String locale;
    private String source;
    private String clientIp;
    private String userAgent;
    private LocalDateTime createdAt;
}
