package com.backend.presentation.mapper;

import com.backend.application.dto.SiteSettingsCommand;
import com.backend.application.dto.SiteSettingsQuery;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.SiteSettingsGlobalDto;
import com.backend.presentation.dto.request.SiteSettingsI18nDto;
import com.backend.presentation.dto.response.SiteSettingsResponseDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper between Presentation layer DTOs and Application layer DTOs
 * This maintains Clean Architecture by preventing direct dependencies
 */
@Component
public class SiteSettingsMapper {

    /**
     * Maps presentation layer request to application layer command
     */
    public SiteSettingsCommand toCommand(Long tenantId, SiteSettingsGlobalDto global, 
                                        Map<String, SiteSettingsI18nDto> languages, Long updatedBy) {
        SiteSettingsCommand.GlobalSettings globalSettings = null;
        if (global != null) {
            globalSettings = new SiteSettingsCommand.GlobalSettings(
                global.contactEmail(),
                global.contactPhone(),
                global.whatsappPhone(),
                global.canonicalBaseUrl(),
                global.robots()
            );
        }

        Map<Language, SiteSettingsCommand.I18nSettings> langSettings = null;
        if (languages != null) {
            langSettings = new HashMap<>();
            for (Map.Entry<String, SiteSettingsI18nDto> entry : languages.entrySet()) {
                Language lang = Language.valueOf(entry.getKey().toUpperCase());
                SiteSettingsI18nDto dto = entry.getValue();
                langSettings.put(lang, new SiteSettingsCommand.I18nSettings(
                    dto.siteName(),
                    dto.tagline(),
                    extractSeoTitle(dto.seo()),
                    extractSeoDescription(dto.seo()),
                    dto.footerText(),
                    dto.headerTopbarText()
                ));
            }
        }

        return new SiteSettingsCommand(tenantId, globalSettings, langSettings, updatedBy);
    }

    /**
     * Maps application layer query result to presentation layer response
     */
    public SiteSettingsResponseDto toResponse(SiteSettingsQuery query) {
        SiteSettingsGlobalDto global = null;
        if (query.global() != null) {
            global = new SiteSettingsGlobalDto(
                query.global().contactEmail(),
                query.global().contactPhone(),
                query.global().whatsappPhone(),
                query.global().canonicalBaseUrl(),
                query.global().robots()
            );
        }

        Map<String, SiteSettingsI18nDto> languages = new HashMap<>();
        if (query.languages() != null) {
            for (Map.Entry<Language, SiteSettingsQuery.I18nSettings> entry : query.languages().entrySet()) {
                String langKey = entry.getKey().name().toLowerCase();
                SiteSettingsQuery.I18nSettings settings = entry.getValue();
                languages.put(langKey, new SiteSettingsI18nDto(
                    settings.siteName(),
                    settings.tagline(),
                    buildSeoJson(settings.seoTitle(), settings.seoDescription()),
                    settings.footerText(),
                    settings.headerTopbarText(),
                    null // addressLocalized - not implemented yet
                ));
            }
        }

        return new SiteSettingsResponseDto(global, languages);
    }

    /**
     * Extracts SEO title from JSON string
     */
    private String extractSeoTitle(String seoJson) {
        if (seoJson == null || seoJson.isEmpty()) {
            return null;
        }
        try {
            // Simple JSON parsing for title field
            if (seoJson.contains("\"title\"")) {
                String[] parts = seoJson.split("\"title\"\\s*:\\s*\"");
                if (parts.length > 1) {
                    String titlePart = parts[1];
                    int endIndex = titlePart.indexOf("\"");
                    if (endIndex > 0) {
                        return titlePart.substring(0, endIndex);
                    }
                }
            }
        } catch (Exception e) {
            // Return null if parsing fails
        }
        return null;
    }

    /**
     * Extracts SEO description from JSON string
     */
    private String extractSeoDescription(String seoJson) {
        if (seoJson == null || seoJson.isEmpty()) {
            return null;
        }
        try {
            // Simple JSON parsing for description field
            if (seoJson.contains("\"description\"")) {
                String[] parts = seoJson.split("\"description\"\\s*:\\s*\"");
                if (parts.length > 1) {
                    String descPart = parts[1];
                    int endIndex = descPart.indexOf("\"");
                    if (endIndex > 0) {
                        return descPart.substring(0, endIndex);
                    }
                }
            }
        } catch (Exception e) {
            // Return null if parsing fails
        }
        return null;
    }

    /**
     * Builds SEO JSON from title and description
     */
    private String buildSeoJson(String title, String description) {
        if (title == null && description == null) {
            return null;
        }
        
        StringBuilder json = new StringBuilder("{");
        if (title != null) {
            json.append("\"title\":\"").append(escapeJson(title)).append("\"");
        }
        if (description != null) {
            if (title != null) {
                json.append(",");
            }
            json.append("\"description\":\"").append(escapeJson(description)).append("\"");
        }
        json.append("}");
        return json.toString();
    }

    /**
     * Escapes JSON special characters
     */
    private String escapeJson(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\"", "\\\"")
                   .replace("\\", "\\\\")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}