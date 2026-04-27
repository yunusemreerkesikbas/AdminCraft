package com.backend.infrastructure.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Public API hardening: trusted proxy headers and contact-form abuse limits.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    /**
     * When true, {@code CF-Connecting-IP} may be used only if {@link #remoteAddrMatchesTrustedProxy(String)}
     * also passes (or {@link #trustedProxyCidrs} is empty, meaning explicit operator opt-in without CIDR checks).
     */
    private boolean trustCfConnectingIp = false;

    /**
     * CIDR blocks for {@code request.getRemoteAddr()} that are allowed to supply {@code CF-Connecting-IP}.
     * When empty and {@link #trustCfConnectingIp} is true, any remote address is accepted (use only behind a
     * terminating proxy you control).
     */
    private List<String> trustedProxyCidrs = new ArrayList<>();

    private int publicContactPerIpPerMinute = 10;

    private int publicContactPerTenantPerMinute = 120;

    private int contactRequestRetentionDays = 90;

    private boolean contactRequestRetentionJobEnabled = true;
}
