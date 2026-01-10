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

        // ========== Navigation Node Entity ==========
        public static final Set<String> NAVIGATION_NODE_ALLOWED_FIELDS = Set.of(
                        "createdAt", "uid", "sortOrder", "title");

        public static final String NAVIGATION_NODE_DEFAULT_SORT = "createdAt,desc";

        public static final List<SortOptionDto> NAVIGATION_NODE_SORT_OPTIONS = List.of(
                        SortOptionDto.defaultOption("createdAt,desc", "admin.sort.newest"),
                        SortOptionDto.of("createdAt,asc", "admin.sort.oldest"),
                        SortOptionDto.of("uid,asc", "admin.sort.uidAsc"),
                        SortOptionDto.of("uid,desc", "admin.sort.uidDesc"),
                        SortOptionDto.of("sortOrder,asc", "admin.sort.orderAsc"),
                        SortOptionDto.of("sortOrder,desc", "admin.sort.orderDesc"),
                        SortOptionDto.of("title,asc", "admin.sort.titleAsc"),
                        SortOptionDto.of("title,desc", "admin.sort.titleDesc"));

        // ========== Page Template Entity ==========
        public static final Set<String> PAGE_TEMPLATE_ALLOWED_FIELDS = Set.of(
                        "id", "createdAt", "name", "uid");

        public static final String PAGE_TEMPLATE_DEFAULT_SORT = "id,desc";

        public static final List<SortOptionDto> PAGE_TEMPLATE_SORT_OPTIONS = List.of(
                        SortOptionDto.defaultOption("id,desc", "admin.sort.newest"),
                        SortOptionDto.of("id,asc", "admin.sort.oldest"),
                        SortOptionDto.of("name,asc", "admin.sort.nameAsc"),
                        SortOptionDto.of("name,desc", "admin.sort.nameDesc"),
                        SortOptionDto.of("uid,asc", "admin.sort.uidAsc"),
                        SortOptionDto.of("uid,desc", "admin.sort.uidDesc"),
                        SortOptionDto.of("createdAt,desc", "admin.sort.createdDesc"),
                        SortOptionDto.of("createdAt,asc", "admin.sort.createdAsc"));

        // ========== Component Entity ==========
        public static final Set<String> COMPONENT_ALLOWED_FIELDS = Set.of(
                        "createdAt", "name", "uid", "displayOrder");

        public static final String COMPONENT_DEFAULT_SORT = "createdAt,desc";

        public static final List<SortOptionDto> COMPONENT_SORT_OPTIONS = List.of(
                        SortOptionDto.defaultOption("createdAt,desc", "admin.sort.newest"),
                        SortOptionDto.of("createdAt,asc", "admin.sort.oldest"),
                        SortOptionDto.of("name,asc", "admin.sort.nameAsc"),
                        SortOptionDto.of("name,desc", "admin.sort.nameDesc"),
                        SortOptionDto.of("displayOrder,asc", "admin.sort.orderAsc"),
                        SortOptionDto.of("displayOrder,desc", "admin.sort.orderDesc"));

        // ========== ComponentType Entity ==========
        public static final Set<String> COMPONENT_TYPE_ALLOWED_FIELDS = Set.of(
                        "createdAt", "name", "uid", "category");

        public static final String COMPONENT_TYPE_DEFAULT_SORT = "createdAt,desc";

        public static final List<SortOptionDto> COMPONENT_TYPE_SORT_OPTIONS = List.of(
                        SortOptionDto.defaultOption("createdAt,desc", "admin.sort.newest"),
                        SortOptionDto.of("createdAt,asc", "admin.sort.oldest"),
                        SortOptionDto.of("name,asc", "admin.sort.nameAsc"),
                        SortOptionDto.of("name,desc", "admin.sort.nameDesc"),
                        SortOptionDto.of("category,asc", "admin.sort.categoryAsc"),
                        SortOptionDto.of("category,desc", "admin.sort.categoryDesc"));

        /**
         * Registry mapping entity names to their allowed fields.
         */
        public static final Map<String, Set<String>> ENTITY_FIELDS = Map.of(
                        "Media", MEDIA_ALLOWED_FIELDS,
                        "NavigationNode", NAVIGATION_NODE_ALLOWED_FIELDS,
                        "PageTemplate", PAGE_TEMPLATE_ALLOWED_FIELDS,
                        "Component", COMPONENT_ALLOWED_FIELDS,
                        "ComponentType", COMPONENT_TYPE_ALLOWED_FIELDS);

        public static final Map<String, List<SortOptionDto>> ENTITY_SORT_OPTIONS = Map.of(
                        "Media", MEDIA_SORT_OPTIONS,
                        "NavigationNode", NAVIGATION_NODE_SORT_OPTIONS,
                        "PageTemplate", PAGE_TEMPLATE_SORT_OPTIONS,
                        "Component", COMPONENT_SORT_OPTIONS,
                        "ComponentType", COMPONENT_TYPE_SORT_OPTIONS);

        public static final Map<String, String> ENTITY_DEFAULT_SORT = Map.of(
                        "Media", MEDIA_DEFAULT_SORT,
                        "NavigationNode", NAVIGATION_NODE_DEFAULT_SORT,
                        "PageTemplate", PAGE_TEMPLATE_DEFAULT_SORT,
                        "Component", COMPONENT_DEFAULT_SORT,
                        "ComponentType", COMPONENT_TYPE_DEFAULT_SORT);
}
