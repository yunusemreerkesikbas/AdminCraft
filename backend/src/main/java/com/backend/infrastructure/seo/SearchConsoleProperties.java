package com.backend.infrastructure.seo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.seo.search-console")
public class SearchConsoleProperties {

    private String apiBaseUrl = "https://searchconsole.googleapis.com/v1";
    private String searchAnalyticsBaseUrl = "https://searchconsole.googleapis.com/webmasters/v3";
    private String scope = "https://www.googleapis.com/auth/webmasters.readonly";
    private String languageCode = "en-US";
    private long cacheTtlSeconds = 300;

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getSearchAnalyticsBaseUrl() {
        return searchAnalyticsBaseUrl;
    }

    public void setSearchAnalyticsBaseUrl(String searchAnalyticsBaseUrl) {
        this.searchAnalyticsBaseUrl = searchAnalyticsBaseUrl;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public long getCacheTtlSeconds() {
        return cacheTtlSeconds;
    }

    public void setCacheTtlSeconds(long cacheTtlSeconds) {
        this.cacheTtlSeconds = cacheTtlSeconds;
    }
}
