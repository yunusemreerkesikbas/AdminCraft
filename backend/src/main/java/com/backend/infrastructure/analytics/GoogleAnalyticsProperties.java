package com.backend.infrastructure.analytics;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "app.analytics.ga4")
public class GoogleAnalyticsProperties {

    private String serviceAccountJson;
    private String serviceAccountJsonBase64;
    private String tokenUri = "https://oauth2.googleapis.com/token";
    private String apiBaseUrl = "https://analyticsdata.googleapis.com/v1beta";
    private String scope = "https://www.googleapis.com/auth/analytics.readonly";
    private long cacheTtlSeconds = 300;

    public String resolveServiceAccountJson() {
        if (StringUtils.hasText(serviceAccountJson)) {
            return serviceAccountJson.trim();
        }
        if (StringUtils.hasText(serviceAccountJsonBase64)) {
            return new String(
                    Base64.getDecoder().decode(serviceAccountJsonBase64.trim()),
                    StandardCharsets.UTF_8);
        }
        return null;
    }

    public String getServiceAccountJson() {
        return serviceAccountJson;
    }

    public void setServiceAccountJson(String serviceAccountJson) {
        this.serviceAccountJson = serviceAccountJson;
    }

    public String getServiceAccountJsonBase64() {
        return serviceAccountJsonBase64;
    }

    public void setServiceAccountJsonBase64(String serviceAccountJsonBase64) {
        this.serviceAccountJsonBase64 = serviceAccountJsonBase64;
    }

    public String getTokenUri() {
        return tokenUri;
    }

    public void setTokenUri(String tokenUri) {
        this.tokenUri = tokenUri;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public long getCacheTtlSeconds() {
        return cacheTtlSeconds;
    }

    public void setCacheTtlSeconds(long cacheTtlSeconds) {
        this.cacheTtlSeconds = cacheTtlSeconds;
    }
}
