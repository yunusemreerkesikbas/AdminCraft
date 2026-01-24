package com.backend.shared.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Utility class for filtering empty/null values in response DTOs.
 * Converts empty values to null so Jackson's NON_NULL configuration can exclude them.
 */
public final class ResponseValueFilter {

    private ResponseValueFilter() {
        // Prevent instantiation
    }

    /**
     * Filters empty strings: returns null if string is null or empty (after trim).
     * Otherwise returns the original string.
     *
     * @param value The string value to filter
     * @return null if empty/whitespace, otherwise the original value
     */
    public static String filterEmptyString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value;
    }

    /**
     * Filters empty collections: returns null if collection is null or empty.
     * Otherwise returns the original collection.
     *
     * @param <T> The collection element type
     * @param collection The collection to filter
     * @return null if null or empty, otherwise the original collection
     */
    public static <T> Collection<T> filterEmptyCollection(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        return collection;
    }

    /**
     * Filters empty lists: returns null if list is null or empty.
     * Otherwise returns the original list.
     *
     * @param <T> The list element type
     * @param list The list to filter
     * @return null if null or empty, otherwise the original list
     */
    public static <T> List<T> filterEmptyList(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list;
    }

    /**
     * Filters empty maps: returns null if map is null or empty.
     * Otherwise returns the original map.
     *
     * @param <K> The map key type
     * @param <V> The map value type
     * @param map The map to filter
     * @return null if null or empty, otherwise the original map
     */
    public static <K, V> Map<K, V> filterEmptyMap(Map<K, V> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        return map;
    }
}
