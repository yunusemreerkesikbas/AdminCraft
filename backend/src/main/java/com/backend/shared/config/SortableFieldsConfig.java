package com.backend.shared.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.backend.presentation.dto.response.SortOptionDto;

/**
 * Configuration for sortable fields per entity.
 * Defines allowed sort fields and their i18n labels.
 */
public final class SortableFieldsConfig {

    private SortableFieldsConfig() {
        // Utility class
    }

    // ========== Media Entity ==========
    public static final Set<String> MEDIA_ALLOWED_FIELDS = Set.of(
            "createdAt", "originalName", "fileSize", "fileType", "mimeType");

    public static final String MEDIA_DEFAULT_SORT = "createdAt,desc";

    public static final List<SortOptionDto> MEDIA_SORT_OPTIONS = List.of(
            SortOptionDto.defaultOption("createdAt,desc", "admin.sort.newest"),
            SortOptionDto.of("createdAt,asc", "admin.sort.oldest"),
            SortOptionDto.of("originalName,asc", "admin.sort.nameAsc"),
            SortOptionDto.of("originalName,desc", "admin.sort.nameDesc"),
            SortOptionDto.of("fileSize,desc", "admin.sort.sizeDesc"),
            SortOptionDto.of("fileSize,asc", "admin.sort.sizeAsc"));

    // ========== Future Entities ==========
    // Add PAGE_ALLOWED_FIELDS, COMPONENT_ALLOWED_FIELDS, etc. here

    /**
     * Registry mapping entity names to their allowed fields.
     */
    public static final Map<String, Set<String>> ENTITY_FIELDS = Map.of(
            "Media", MEDIA_ALLOWED_FIELDS);

    public static final Map<String, List<SortOptionDto>> ENTITY_SORT_OPTIONS = Map.of(
            "Media", MEDIA_SORT_OPTIONS);

    public static final Map<String, String> ENTITY_DEFAULT_SORT = Map.of(
            "Media", MEDIA_DEFAULT_SORT);
}
